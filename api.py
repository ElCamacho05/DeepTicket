import re
from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn

# Inicializamos la API
app = FastAPI(title="API Backend - Escáner de Tickets")

# 1. Definimos el "molde" para lo que la API va a recibir
# El modelo de ML arrojará un texto largo, así que esperamos un string.
class TicketInput(BaseModel):
    raw_text: str

# 2. La función exacta que mandó tu compañero al grupo
def parse_sroie_output(raw: str) -> dict:
    """Extrae campos y tabla de productos del output del modelo SROIE."""
    def extract(pattern, text):
        m = re.search(pattern, text, re.DOTALL)
        return m.group(1).strip() if m else "-"

    # Campos simples
    parsed = {
        "Empresa": extract(r"<s_company>(.*?)</s_company>", raw),
        "Fecha":   extract(r"<s_date>(.*?)</s_date>", raw),
        "Dirección": extract(r"<s_address>(.*?)</s_address>", raw),
        "Total":   extract(r"<s_total>(.*?)</s_total>", raw),
    }

    # ===== PRODUCTOS =====
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

# 3. El Endpoint que te pidió Camacho
@app.post("/parse-ticket")
def procesar_ticket(ticket: TicketInput):
    # Tomamos el texto crudo que nos envían y se lo pasamos a la función
    resultado = parse_sroie_output(ticket.raw_text)
    
    # Devolvemos el diccionario (FastAPI lo convierte a JSON automáticamente)
    return {
        "status": "success",
        "datos_extraidos": resultado
    }

# Bloque de ejecución (útil para correrlo en local)
if __name__ == "__main__":
    # Suponiendo que el archivo se llama main.py
    uvicorn.run("api:app", host="0.0.0.0", port=8000, reload=True)
