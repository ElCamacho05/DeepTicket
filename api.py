from supabase import create_client, Client
import os
import re
import torch
import random
import pandas as pd
import numpy as np
import shutil
from PIL import Image
from transformers import DonutProcessor, VisionEncoderDecoderModel
from fastapi import FastAPI, UploadFile, File, Form
from sklearn.preprocessing import MinMaxScaler
from sklearn.neighbors import NearestNeighbors
from thefuzz import process, fuzz
from pydantic import BaseModel
from typing import List

# ==========================================
# INICIALIZACIÓN DE API Y SUPABASE
# ==========================================
app = FastAPI(title="API Backend - Escáner y Recomendador DeepTicket")

URL_SUPABASE = "https://kqwuxnejznikpcqvagul.supabase.co"
KEY_SUPABASE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtxd3V4bmVqem5pa3BjcXZhZ3VsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU0Mzc4MTYsImV4cCI6MjA5MTAxMzgxNn0.2aK62lF0vquIMLvOa8R4Tc-PHBZFvNKwpK64R1DVv7E"
supabase: Client = create_client(URL_SUPABASE, KEY_SUPABASE)

# Memoria Caché Global para no sobrecargar Supabase en cada petición
df_maestro_global = pd.DataFrame()

# ==========================================
# EVENTO DE ARRANQUE (PAGINACIÓN SUPABASE)
# ==========================================
@app.on_event("startup")
def startup_event():
    global df_maestro_global
    print("📡 Descargando base de datos maestra de Supabase (Paginación actiada)...")

    all_data = []
    chunk_size = 1000
    start = 0

    # Bucle para saltar el límite de 1000 de Supabase y descargar todo
    while True:
        res = supabase.table("tickets").select("*").range(start, start + chunk_size - 1).execute()
        if not res.data:
            break
        all_data.extend(res.data)
        if len(res.data) < chunk_size:
            break
        start += chunk_size

    if all_data:
        df_maestro_global = pd.DataFrame(all_data)
        print(f"✅ ¡Catálogo descargado con éxito! {len(df_maestro_global)} registros en memoria.")
    else:
        print("⚠️ Advertencia: Base de datos en Supabase está vacía.")

# ==========================================
# 1. CARGAMOS EL MODELO DONUT (CEREBRO VISUAL)
# ==========================================
print("🧠 Cargando cerebro visual (Donut)... Esto puede tardar unos segundos.")
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
RUTA_DONUT = "Modelos/EscanerTickets/"

try:
    processor = DonutProcessor.from_pretrained(RUTA_DONUT)
    model_donut = VisionEncoderDecoderModel.from_pretrained(RUTA_DONUT, torch_dtype=torch.bfloat16).to(DEVICE)
    model_donut.eval()
    print("✅ ¡Modelo OCR cargado!")
except Exception as e:
    print(f"⚠️ Error al cargar OCR: {e}")
    processor = None
    model_donut = None

# ==========================================
# 2. MOTOR DE TRADUCCIÓN (THEFUZZ)
# ==========================================
def categorizar_producto_robusto(nombre_escaneado, df_bd):
    nombre_escaneado = str(nombre_escaneado).lower().strip()

    if df_bd.empty or 'Product Name' not in df_bd.columns:
        return nombre_escaneado, "Otros"

    productos_perfectos = df_bd['Product Name'].dropna().unique()
    if len(productos_perfectos) == 0:
        return nombre_escaneado, "Otros"

    umbral_actual = 85 if len(nombre_escaneado) <= 4 else 70

    mejor_coincidencia, puntaje = process.extractOne(
        nombre_escaneado,
        productos_perfectos,
        scorer=fuzz.token_set_ratio
    )

    if puntaje >= umbral_actual:
        # Obtenemos la categoría de ese producto exacto
        categoria = df_bd[df_bd['Product Name'] == mejor_coincidencia]['Category'].iloc[0]
        return mejor_coincidencia, categoria

    return nombre_escaneado, "Otros"

def parse_sroie_output(texto_crudo: str, df_bd: pd.DataFrame):
    texto_crudo = texto_crudo.replace("<s_cord-v2>", "").replace("</s_cord-v2>", "")
    partes = texto_crudo.split("<s_total>")

    bloque_menu = partes[0] if len(partes) > 0 else ""
    productos_encontrados = []

    # ---------------------------------------------------------
    # EXTRACTOR "ASPIRADORA" (Idéntico a TestSistema.ipynb)
    # ---------------------------------------------------------
    textos_crudos = re.findall(r">([^<]+)<", bloque_menu)
    textos_limpios = [t.strip() for t in textos_crudos if t.strip()]

    nombres_potenciales = []
    precios_potenciales = []

    for t in textos_limpios:
        if re.match(r"^[\d\.,\s\-\+x\$]+$", t):
            precios_potenciales.append(t)
        elif len(t) > 2:
            nombres_potenciales.append(t)

    # Filtro TheFuzz para limpiar basuras visuales
    for nombre_crudo in set(nombres_potenciales):
        nombre_real, categoria_real = categorizar_producto_robusto(nombre_crudo, df_bd)

        # Si TheFuzz confirma que existe en la BD maestra
        if categoria_real != "Otros":
            precio_asignado = precios_potenciales.pop(0) if precios_potenciales else "0.0"
            productos_encontrados.append({
                "Producto": nombre_real,
                "Categoria": categoria_real,
                "Cantidad": "1",
                "Precio": precio_asignado
            })

    return {
        "Empresa": "Supermercado (Autodetectado)",
        "_productos": productos_encontrados
    }

@app.post("/parse-ticket")
async def procesar_ticket(
        file: UploadFile = File(...),
        customer_id: str = Form(...),
        customer_name: str = Form(...)
):
    global df_maestro_global

    if not processor or not model_donut:
        return {"status": "error", "message": "El modelo no está cargado."}

    temp_file_path = f"temp_{file.filename}"
    with open(temp_file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    # Escaneo IA
    imagen = Image.open(temp_file_path).convert("RGB")
    pixel_values = processor(imagen, return_tensors="pt").pixel_values.to(DEVICE, dtype=model_donut.dtype)
    decoder_input_ids = processor.tokenizer("<s_cord-v2>", add_special_tokens=False, return_tensors="pt").input_ids.to(DEVICE)

    outputs = model_donut.generate(
        pixel_values, decoder_input_ids=decoder_input_ids, max_length=512,
        pad_token_id=processor.tokenizer.pad_token_id, eos_token_id=processor.tokenizer.eos_token_id
    )

    secuencia = processor.batch_decode(outputs)[0]
    texto_leido_por_ia = secuencia.replace(processor.tokenizer.eos_token, "").replace(processor.tokenizer.pad_token, "")

    # Limpieza (Pasamos el DataFrame completo de memoria para buscar matches)
    resultado = parse_sroie_output(texto_leido_por_ia, df_maestro_global)
    empresa = resultado.get("Empresa", "Supermercado")

    # Inserción a Supabase
    try:
        nuevos_registros = []
        for producto in resultado.get("_productos", []):
            precio_limpio = str(producto.get("Precio", "0.0")).replace("$", "").replace(",", "")
            try:
                precio_float = float(precio_limpio)
            except:
                precio_float = 0.0

            nuevo_registro = {
                "Order ID": f"TIC-{os.urandom(4).hex().upper()}",
                "Customer ID": customer_id,
                "Customer Name": customer_name,
                "Sub-Category": empresa,
                "Precio_Total": precio_float,
                "Quantity": 1,
                "Edad": 25,
                "Genero": "No especificado",
                "Ingresos_Anuales": 0.0,
                "Education": "N/A",
                "Marital_Status": "Single",
                "Tipo_Comercio": empresa,
                "Category": producto.get("Categoria"),
                "Product Name": producto.get("Producto")
            }
            # Guardar en Supabase Nube
            supabase.table("tickets").insert(nuevo_registro).execute()
            nuevos_registros.append(nuevo_registro)

        # Actualizamos nuestra caché RAM al instante para que la IA aprenda
        if nuevos_registros:
            df_maestro_global = pd.concat([df_maestro_global, pd.DataFrame(nuevos_registros)], ignore_index=True)

    except Exception as e:
        print(f"Error guardando en Supabase: {e}")

    if os.path.exists(temp_file_path):
        os.remove(temp_file_path)

    return {"status": "success", "datos_extraidos": resultado}


# ==========================================
# 3. EL MOTOR DE RECOMENDACIÓN (KNN HÍBRIDO)
# ==========================================
class RecommendationRequest(BaseModel):
    user_id: str
    categorias_visibles: List[str]

@app.post("/recommend")
def get_recommendations(req: RecommendationRequest):
    global df_maestro_global

    df_maestro = df_maestro_global.copy()
    if df_maestro.empty:
        return {"recommendations": []}

    # 1. Matriz con Penalización IUF (Identica a Metricas.ipynb)
    matriz_compras = pd.crosstab(df_maestro['Customer ID'], df_maestro['Category'])
    item_popularity = (matriz_compras > 0).sum(axis=0)
    total_users = matriz_compras.shape[0]

    iuf_penalty = (np.log(total_users / (item_popularity + 1))) ** 2
    matriz_compras_penalizada = matriz_compras * iuf_penalty.values
    matriz_compras_norm = matriz_compras_penalizada.copy()

    # 2. Limpieza Demográfica
    df_maestro['Edad'] = pd.to_numeric(df_maestro['Edad'], errors='coerce').fillna(25)
    df_maestro['Ingresos_Anuales'] = pd.to_numeric(df_maestro['Ingresos_Anuales'], errors='coerce').fillna(0)

    df_demograficos = df_maestro[['Customer ID', 'Edad', 'Genero', 'Ingresos_Anuales']].drop_duplicates(subset=['Customer ID']).set_index('Customer ID')
    df_genero = pd.get_dummies(df_demograficos['Genero'], prefix='Gen').astype(float)

    scaler_demo = MinMaxScaler()
    df_numericos = pd.DataFrame(
        scaler_demo.fit_transform(df_demograficos[['Edad', 'Ingresos_Anuales']]),
        index=df_demograficos.index,
        columns=['Edad_Norm', 'Ingresos_Norm']
    )

    # 3. Fusión Híbrida y Pesos Invertidos (Compras x2.0, Demografía x0.5)
    matriz_hibrida = pd.concat([matriz_compras_norm, df_genero, df_numericos], axis=1).fillna(0)

    for col in matriz_compras_norm.columns:
        matriz_hibrida[col] *= 2.0
    matriz_hibrida[['Edad_Norm', 'Ingresos_Norm']] *= 0.5
    for col in df_genero.columns:
        matriz_hibrida[col] *= 0.5

    # 4. Entrenamos el modelo
    vecinos_a_buscar = min(31, len(matriz_hibrida))
    if vecinos_a_buscar < 2:
        return {"recommendations": []}

    modelo_knn = NearestNeighbors(metric='cosine', algorithm='brute', n_neighbors=vecinos_a_buscar)
    modelo_knn.fit(matriz_hibrida)

    # 5. El vector del usuario
    vector_busqueda = pd.DataFrame(0.0, index=[req.user_id], columns=matriz_hibrida.columns)

    if req.user_id in matriz_hibrida.index:
        vector_busqueda.loc[req.user_id] = matriz_hibrida.loc[req.user_id]
        compras_historicas = df_maestro[df_maestro['Customer ID'] == req.user_id]['Product Name'].unique()
    else:
        compras_historicas = []

    # Le damos un Boost a lo que acaba de escanear
    for cat in req.categorias_visibles:
        if cat in vector_busqueda.columns:
            vector_busqueda.at[req.user_id, cat] += 2.0

            # 6. Buscamos Tribu
    _, indices = modelo_knn.kneighbors(vector_busqueda, n_neighbors=vecinos_a_buscar)
    vecinos_reales = [idx for idx in indices[0] if matriz_hibrida.index[idx] != req.user_id]
    vecinos_ids = matriz_hibrida.index[vecinos_reales].tolist()

    # 7. Extraemos compras recomendables
    compras_tribu = df_maestro[df_maestro['Customer ID'].isin(vecinos_ids)]['Product Name']
    recomendaciones_limpias = compras_tribu[~compras_tribu.isin(compras_historicas)]

    # 8. LA VENTANA DE VARIABILIDAD
    mejores_candidatos = recomendaciones_limpias.value_counts().head(12).index.tolist()

    if len(mejores_candidatos) > 5:
        top_recomendaciones = random.sample(mejores_candidatos, 5)
    else:
        top_recomendaciones = mejores_candidatos

    # 9. Formateamos la respuesta para Android
    resultado_json = []
    for item in top_recomendaciones:
        coincidencias = df_maestro[df_maestro['Product Name'] == item]
        cat = coincidencias['Category'].iloc[0] if not coincidencias.empty else "Otros"
        resultado_json.append({"productName": item, "category": cat, "score": 100})

    return {"recommendations": resultado_json}