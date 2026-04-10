from supabase import create_client, Client
import os
import re
import os
import shutil
import torch
from PIL import Image
from transformers import DonutProcessor, VisionEncoderDecoderModel
from fastapi import FastAPI, UploadFile, File, Form
import uvicorn

# Inicializamos la API
app = FastAPI(title="API Backend - Escáner de Tickets")

URL_SUPABASE = "https://kqwuxnejznikpcqvagul.supabase.co"
KEY_SUPABASE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtxd3V4bmVqem5pa3BjcXZhZ3VsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU0Mzc4MTYsImV4cCI6MjA5MTAxMzgxNn0.2aK62lF0vquIMLvOa8R4Tc-PHBZFvNKwpK64R1DVv7E"

supabase: Client = create_client(URL_SUPABASE, KEY_SUPABASE)

# ==========================================
# 1. CARGAMOS EL MODELO (SÓLO UNA VEZ AL INICIO)
# ==========================================
print("Cargando cerebro visual (Donut)... Esto puede tardar unos segundos.")
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

# ¡OJO AQUÍ! Asegúrate de que esta ruta sea correcta dependiendo de dónde ejecutes api.py
RUTA_DONUT = "Modelos/EscanerTickets/" 

try:
    processor = DonutProcessor.from_pretrained(RUTA_DONUT)
    # Nota: Si te marca error el 'bfloat16' en tu compu, simplemente quita el parámetro 'torch_dtype=torch.bfloat16'
    model_donut = VisionEncoderDecoderModel.from_pretrained(RUTA_DONUT, torch_dtype=torch.bfloat16).to(DEVICE)
    model_donut.eval()
    print("¡Modelo cargado y listo para escanear!")
except Exception as e:
    print(f"⚠️ Error al cargar el modelo: {e}")
    processor = None
    model_donut = None


# ==========================================
# 2. FUNCIÓN DE LIMPIEZA (La que tú hiciste)
# ==========================================
def parse_sroie_output(raw: str) -> dict:
    """Extrae campos y tabla de productos del output del modelo SROIE."""
    def extract(pattern, text):
        m = re.search(pattern, text, re.DOTALL)
        return m.group(1).strip() if m else "-"

    parsed = {
        "Empresa": extract(r"<s_company>(.*?)</s_company>", raw),
        "Fecha":   extract(r"<s_date>(.*?)</s_date>", raw),
        "Dirección": extract(r"<s_address>(.*?)</s_address>", raw),
        "Total":   extract(r"<s_total>(.*?)</s_total>", raw),
    }

    menu_block = extract(r"<s_menu>(.*?)</s_menu>", raw)
    items = re.findall(r"<s_item>(.*?)</s_item>", menu_block, re.DOTALL)
    productos = []
    for item in items:
        productos.append({
            "Producto": extract(r"<s_nm>(.*?)</s_nm>", item),
            "Cantidad": extract(r"<s_cnt>(.*?)</s_cnt>", item),
            "Precio":   extract(r"<s_price>(.*?)</s_price>", item),
        })
    parsed["_productos"] = productos
    
    return parsed


# ==========================================
# 3. EL ENDPOINT QUE RECIBE LA FOTO DE KOTLIN
# ==========================================
@app.post("/parse-ticket")

@app.post("/parse-ticket")
async def procesar_ticket(
        file: UploadFile = File(...),
        customer_id: str = Form(...),   # Recibimos el ID desde Android
        customer_name: str = Form(...)  # Recibimos el Nombre desde Android
):
    # Guardamos la foto temporalmente
    temp_file_path = f"temp_{file.filename}"
    with open(temp_file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

        print("foto guardada")
    
    if model_donut is None:
        return {"status": "error", "mensaje": "El modelo no está cargado en el servidor."}

    # --- AQUÍ EMPIEZA LA MAGIA DE LA IA ---
    # 1. Abrimos la imagen
    imagen = Image.open(temp_file_path).convert("RGB")
    
    # 2. La preparamos para que Donut la entienda
    pixel_values = processor(imagen, return_tensors="pt").pixel_values.to(DEVICE, dtype=model_donut.dtype)
    decoder_input_ids = processor.tokenizer("<s_cord-v2>", add_special_tokens=False, return_tensors="pt").input_ids.to(DEVICE)
    
    # 3. Generamos el texto leyendo la imagen
    outputs = model_donut.generate(
        pixel_values, 
        decoder_input_ids=decoder_input_ids, 
        max_length=512,

        pad_token_id=processor.tokenizer.pad_token_id, 
        eos_token_id=processor.tokenizer.eos_token_id
    )
    
    # 4. Decodificamos el resultado para quitarle etiquetas basura
    secuencia = processor.batch_decode(outputs)[0]
    texto_leido_por_ia = secuencia.replace(processor.tokenizer.eos_token, "").replace(processor.tokenizer.pad_token, "")
    # --- FIN DE LA IA ---

    print(f"Texto crudo detectado: {texto_leido_por_ia}")

    # Limpiamos el texto con tu función
    resultado = parse_sroie_output(texto_leido_por_ia)
    
    # --- INSERCIÓN EN LA BASE DE DATOS (SUPABASE) ---
    empresa = resultado.get("Empresa", "Supermercado")
    
    try:
        # Por cada producto encontrado en el ticket, insertamos un registro
        for producto in resultado.get("_productos", []):
            
            # Limpiamos el precio por si trae signos de dólar o texto raro
            precio_limpio = producto.get("Precio", "0.0").replace("$", "").replace(",", "")
            try:
                precio_float = float(precio_limpio)
            except:
                precio_float = 0.0

            nombre_del_producto = producto.get("Producto", "Desconocido")

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
                # Como lo pediste: Product Name y Category reciben exactamente lo mismo
                "Category": nombre_del_producto,
                "Product Name": nombre_del_producto
            }
            # Insertamos en tu tabla 'tickets'
            supabase.table("tickets").insert(nuevo_registro).execute()
            print(nuevo_registro)
            
    except Exception as e:
        print(f"Error guardando en Supabase: {e}")
    # --- FIN DE INSERCIÓN ---

    # Borramos la foto temporal
    if os.path.exists(temp_file_path):
        os.remove(temp_file_path)
    
    return {
        "status": "success",
        "datos_extraidos": resultado
    }

if __name__ == "__main__":
    uvicorn.run("api:app", host="0.0.0.0", port=8000, reload=False)
