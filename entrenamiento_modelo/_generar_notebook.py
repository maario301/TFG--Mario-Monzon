# Generador del notebook de Colab (produce un .ipynb válido sin escribir JSON a mano)
import json

def md(*lines):
    return {"cell_type": "markdown", "metadata": {}, "source": list(lines)}

def code(*lines):
    return {"cell_type": "code", "metadata": {}, "execution_count": None,
            "outputs": [], "source": list(lines)}

ESPECIES = [
 "Agkistrodon_piscivorus","Androctonus_australis","Asthenosoma_varium","Atrax_robustus",
 "Berberomeloe_majalis","Bitis_gabonica","Bungarus_caeruleus","Calloselasma_rhodostoma",
 "Carukia_barnesi","Centruroides_sculpturatus","Chironex_fleckeri","Conus_geographus",
 "Crotalus_adamanteus","Daboia_russelii","Dasyatis_pastinaca","Dendroaspis_polylepis",
 "Dendrobates_tinctorius","Enhydrina_schistosa","Hadronyche_formidabilis","Hapalochlaena_maculosa",
 "Heloderma_suspectum","Latrodectus_mactans","Leiurus_quinquestriatus","Lonomia_obliqua",
 "Loxosceles_laeta","Micrurus_fulvius","Naja_naja","Naja_nigricollis","Notechis_scutatus",
 "Nycticebus_coucang","Ophiophagus_hannah","Ornithorhynchus_anatinus","Oxyuranus_microlepidotus",
 "Paraponera_clavata","Phoneutria_nigriventer","Phyllobates_terribilis","Phyllomedusa_sauvagii",
 "Physalia_physalis","Pitohui_dichrous","Pterois_volitans","Salamandra_salamandra",
 "Scolopendra_cingulata","Scolopendra_gigantea","Steatoda_nobilis","Synanceia_horrida",
 "Takifugu_rubripes","Varanus_komodoensis","Vespa_mandarinia","Vipera_latastei",
]
especies_py = "ESPECIES = [\n"
for i in range(0, len(ESPECIES), 4):
    especies_py += "    " + ", ".join('"%s"' % e for e in ESPECIES[i:i+4]) + ",\n"
especies_py += "]\n"

cells = []

cells.append(md(
    "# VenoMap — Reentrenamiento del modelo de IA\n",
    "\n",
    "Notebook para **Google Colab**. Descarga fotos reales de **iNaturalist**, entrena con\n",
    "*transfer learning* (MobileNetV2) y exporta `model_unquant.tflite` + `labels.txt`\n",
    "listos para tu app, **sin tocar el código Kotlin**.\n",
    "\n",
    "### Antes de empezar\n",
    "1. Menú **Entorno de ejecución → Cambiar tipo de entorno de ejecución → GPU**.\n",
    "2. Ejecuta las celdas **en orden** (Shift+Enter).\n",
    "3. La descarga es lo más lento (respeta el límite de la API de iNaturalist).\n"
))

cells.append(md("## 1. Configuración"))
cells.append(code(
    "# Ajusta estos valores si quieres\n",
    "IMAGENES_POR_ESPECIE = 150   # sube a 300 para más calidad (tarda más)\n",
    "TAM_IMG = 224                # entrada del modelo (NO cambiar: la app espera 224)\n",
    "EPOCAS_BASE = 12             # épocas con la base congelada\n",
    "EPOCAS_FINETUNE = 8          # épocas afinando las últimas capas\n",
    "LOTE = 32\n",
    "\n",
    "import os, time, requests\n",
    "import tensorflow as tf\n",
    "print('TensorFlow', tf.__version__)\n",
    "print('GPU disponible:', tf.config.list_physical_devices('GPU'))\n"
))

cells.append(md("## 2. Lista de especies\n",
                "Mismo orden alfabético que tu `labels.txt`, **sin** la clase \"Otros\"\n",
                "(esa la sigue dando el umbral de confianza de la app)."))
cells.append(code(especies_py, "print(len(ESPECIES), 'especies')\n"))

cells.append(md("## 3. Descargar imágenes de iNaturalist\n",
                "Busca cada especie por su nombre científico y baja las fotos mejor valoradas\n",
                "de observaciones *research grade*."))
cells.append(code(
    "DIR_DATOS = '/content/dataset'\n",
    "os.makedirs(DIR_DATOS, exist_ok=True)\n",
    "\n",
    "def taxon_id(nombre_cientifico):\n",
    "    r = requests.get('https://api.inaturalist.org/v1/taxa',\n",
    "                     params={'q': nombre_cientifico, 'rank': 'species', 'per_page': 1})\n",
    "    res = r.json().get('results', [])\n",
    "    return res[0]['id'] if res else None\n",
    "\n",
    "def descargar_especie(nombre, objetivo):\n",
    "    carpeta = os.path.join(DIR_DATOS, nombre)\n",
    "    os.makedirs(carpeta, exist_ok=True)\n",
    "    descargadas = len(os.listdir(carpeta))\n",
    "    if descargadas >= objetivo:\n",
    "        print(f'  {nombre}: ya hay {descargadas}, salto'); return\n",
    "    tid = taxon_id(nombre.replace('_', ' '))\n",
    "    if not tid:\n",
    "        print(f'  {nombre}: NO encontrado en iNaturalist'); return\n",
    "    pagina = 1\n",
    "    while descargadas < objetivo and pagina <= 10:\n",
    "        r = requests.get('https://api.inaturalist.org/v1/observations',\n",
    "            params={'taxon_id': tid, 'photos': 'true', 'quality_grade': 'research',\n",
    "                    'per_page': 200, 'page': pagina, 'order_by': 'votes'})\n",
    "        obs = r.json().get('results', [])\n",
    "        if not obs: break\n",
    "        for o in obs:\n",
    "            for p in o.get('photos', []):\n",
    "                if descargadas >= objetivo: break\n",
    "                url = p['url'].replace('square', 'medium')\n",
    "                try:\n",
    "                    img = requests.get(url, timeout=15).content\n",
    "                    with open(os.path.join(carpeta, f'{descargadas}.jpg'), 'wb') as f:\n",
    "                        f.write(img)\n",
    "                    descargadas += 1\n",
    "                except Exception:\n",
    "                    pass\n",
    "        pagina += 1\n",
    "        time.sleep(1)  # respetar el limite de la API (1 req/seg)\n",
    "    print(f'  {nombre}: {descargadas} imagenes')\n",
    "\n",
    "for i, esp in enumerate(ESPECIES):\n",
    "    print(f'[{i+1}/{len(ESPECIES)}] {esp}')\n",
    "    descargar_especie(esp, IMAGENES_POR_ESPECIE)\n",
    "print('Descarga terminada')\n"
))

cells.append(md("## 4. Limpieza y recuento\n",
                "Borra imágenes corruptas y enseña cuántas hay por especie."))
cells.append(code(
    "import PIL.Image\n",
    "total = 0\n",
    "for esp in sorted(os.listdir(DIR_DATOS)):\n",
    "    carpeta = os.path.join(DIR_DATOS, esp)\n",
    "    buenas = 0\n",
    "    for fn in list(os.listdir(carpeta)):\n",
    "        ruta = os.path.join(carpeta, fn)\n",
    "        try:\n",
    "            PIL.Image.open(ruta).verify(); buenas += 1\n",
    "        except Exception:\n",
    "            os.remove(ruta)\n",
    "    total += buenas\n",
    "    aviso = '  <-- POCAS' if buenas < 40 else ''\n",
    "    print(f'{esp}: {buenas}{aviso}')\n",
    "print('TOTAL imagenes:', total)\n"
))

cells.append(md("## 5. Preparar los datasets (train / validación + aumento)"))
cells.append(code(
    "from tensorflow.keras.applications.mobilenet_v2 import preprocess_input\n",
    "\n",
    "train_ds = tf.keras.utils.image_dataset_from_directory(\n",
    "    DIR_DATOS, validation_split=0.2, subset='training', seed=123,\n",
    "    image_size=(TAM_IMG, TAM_IMG), batch_size=LOTE, label_mode='categorical')\n",
    "val_ds = tf.keras.utils.image_dataset_from_directory(\n",
    "    DIR_DATOS, validation_split=0.2, subset='validation', seed=123,\n",
    "    image_size=(TAM_IMG, TAM_IMG), batch_size=LOTE, label_mode='categorical')\n",
    "\n",
    "CLASES = train_ds.class_names\n",
    "print(len(CLASES), 'clases')\n",
    "\n",
    "aumento = tf.keras.Sequential([\n",
    "    tf.keras.layers.RandomFlip('horizontal'),\n",
    "    tf.keras.layers.RandomRotation(0.15),\n",
    "    tf.keras.layers.RandomZoom(0.15),\n",
    "    tf.keras.layers.RandomContrast(0.1),\n",
    "])\n",
    "\n",
    "AUTOTUNE = tf.data.AUTOTUNE\n",
    "def preparar(ds, entrenamiento=False):\n",
    "    if entrenamiento:\n",
    "        ds = ds.map(lambda x, y: (aumento(x), y), num_parallel_calls=AUTOTUNE)\n",
    "    # preprocess_input normaliza a [-1, 1] = lo mismo que hace la app\n",
    "    ds = ds.map(lambda x, y: (preprocess_input(x), y), num_parallel_calls=AUTOTUNE)\n",
    "    return ds.prefetch(AUTOTUNE)\n",
    "\n",
    "train_p = preparar(train_ds, True)\n",
    "val_p = preparar(val_ds, False)\n"
))

cells.append(md("## 6. Entrenar (base congelada)"))
cells.append(code(
    "base = tf.keras.applications.MobileNetV2(input_shape=(TAM_IMG, TAM_IMG, 3),\n",
    "                                         include_top=False, weights='imagenet')\n",
    "base.trainable = False\n",
    "\n",
    "modelo = tf.keras.Sequential([\n",
    "    base,\n",
    "    tf.keras.layers.GlobalAveragePooling2D(),\n",
    "    tf.keras.layers.Dropout(0.3),\n",
    "    tf.keras.layers.Dense(len(CLASES), activation='softmax'),\n",
    "])\n",
    "modelo.compile(optimizer=tf.keras.optimizers.Adam(1e-3),\n",
    "               loss='categorical_crossentropy', metrics=['accuracy'])\n",
    "modelo.fit(train_p, validation_data=val_p, epochs=EPOCAS_BASE)\n"
))

cells.append(md("## 7. Afinar (fine-tuning de las últimas capas)"))
cells.append(code(
    "base.trainable = True\n",
    "for capa in base.layers[:-30]:\n",
    "    capa.trainable = False\n",
    "modelo.compile(optimizer=tf.keras.optimizers.Adam(1e-5),\n",
    "               loss='categorical_crossentropy', metrics=['accuracy'])\n",
    "modelo.fit(train_p, validation_data=val_p, epochs=EPOCAS_FINETUNE)\n"
))

cells.append(md("## 8. Exportar a TFLite + labels.txt\n",
                "El modelo espera entrada normalizada a `[-1, 1]`, exactamente lo que hace tu app."))
cells.append(code(
    "conv = tf.lite.TFLiteConverter.from_keras_model(modelo)\n",
    "tflite = conv.convert()\n",
    "with open('model_unquant.tflite', 'wb') as f:\n",
    "    f.write(tflite)\n",
    "with open('labels.txt', 'w') as f:\n",
    "    f.write('\\n'.join(CLASES))\n",
    "print('Generados model_unquant.tflite y labels.txt')\n",
    "print('Orden de clases:', CLASES)\n"
))

cells.append(md("## 9. Descargar los archivos"))
cells.append(code(
    "from google.colab import files\n",
    "files.download('model_unquant.tflite')\n",
    "files.download('labels.txt')\n"
))

cells.append(md(
    "## Cómo meterlo en la app\n",
    "1. Sustituye `AppVenenos/app/src/main/assets/model_unquant.tflite` por el nuevo.\n",
    "2. Sustituye `AppVenenos/app/src/main/assets/labels.txt` por el nuevo (49 líneas).\n",
    "3. Recompila la APK. **No hay que tocar código.**\n",
    "\n",
    "La clase \"Otros\" la da el umbral de confianza de la app (si la mejor < 70%).\n"
))

notebook = {
    "nbformat": 4, "nbformat_minor": 0,
    "metadata": {
        "colab": {"provenance": []},
        "kernelspec": {"name": "python3", "display_name": "Python 3"},
        "accelerator": "GPU",
    },
    "cells": cells,
}

salida = "VenoMap_entrenar_modelo.ipynb"
with open(salida, "w", encoding="utf-8") as f:
    json.dump(notebook, f, ensure_ascii=False, indent=1)
print("Notebook generado:", salida, "con", len(cells), "celdas")
