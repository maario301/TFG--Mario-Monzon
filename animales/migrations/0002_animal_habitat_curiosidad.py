# -*- coding: utf-8 -*-
from django.db import migrations, models


# Hábitat natural + curiosidad (1-2 líneas) de cada especie del catálogo.
# Generado para el "Animal del día". Revisa el rigor de los datos si es necesario.
DATOS = {
    "Agkistrodon_piscivorus": {
        "habitat": "Humedales, pantanos y orillas de ríos del sureste de Estados Unidos.",
        "curiosidad": "También se le llama 'boca de algodón' porque abre la boca mostrando su interior blanco como advertencia.",
    },
    "Androctonus_australis": {
        "habitat": "Desiertos y zonas áridas del norte de África y Oriente Medio.",
        "curiosidad": "Es uno de los escorpiones más letales del mundo: su veneno puede matar a una persona en pocas horas.",
    },
    "Asthenosoma_varium": {
        "habitat": "Arrecifes y fondos marinos del Indo-Pacífico.",
        "curiosidad": "Sus espinas llevan sacos de veneno azulados y debe su nombre al dolor ardiente que provoca.",
    },
    "Atrax_robustus": {
        "habitat": "Zonas húmedas y jardines del este de Australia, alrededor de Sídney.",
        "curiosidad": "Es de las arañas más peligrosas del mundo, pero desde que existe antiveneno (1981) no ha habido muertes.",
    },
    "Berberomeloe_majalis": {
        "habitat": "Praderas y zonas secas de la península ibérica.",
        "curiosidad": "Segrega cantaridina, una sustancia tóxica capaz de provocar ampollas si se toca.",
    },
    "Bitis_gabonica": {
        "habitat": "Selvas tropicales y bosques de África central y occidental.",
        "curiosidad": "Tiene los colmillos más largos de cualquier serpiente (hasta 5 cm) y un camuflaje casi perfecto entre la hojarasca.",
    },
    "Bungarus_caeruleus": {
        "habitat": "Campos y zonas rurales del subcontinente indio.",
        "curiosidad": "Muerde sobre todo de noche y casi no duele, por lo que muchas víctimas no se dan cuenta a tiempo.",
    },
    "Calloselasma_rhodostoma": {
        "habitat": "Plantaciones y bosques del sudeste asiático.",
        "curiosidad": "De su veneno se obtuvo la ancrod, usada en medicina como anticoagulante.",
    },
    "Carukia_barnesi": {
        "habitat": "Aguas costeras del norte de Australia.",
        "curiosidad": "Mide apenas 1 cm pero provoca el 'síndrome de Irukandji', con dolor extremo y sensación de muerte inminente.",
    },
    "Centruroides_sculpturatus": {
        "habitat": "Desiertos del suroeste de EE. UU. y norte de México.",
        "curiosidad": "Es el escorpión más venenoso de Norteamérica y brilla con luz ultravioleta.",
    },
    "Chironex_fleckeri": {
        "habitat": "Aguas costeras del norte de Australia y el Indo-Pacífico.",
        "curiosidad": "Es el animal marino más venenoso conocido: su picadura puede ser mortal en pocos minutos.",
    },
    "Conus_geographus": {
        "habitat": "Arrecifes de coral del Indo-Pacífico.",
        "curiosidad": "Dispara un 'arpón' venenoso; su toxina se investiga como analgésico mil veces más potente que la morfina.",
    },
    "Crotalus_adamanteus": {
        "habitat": "Bosques de pinos y matorrales del sureste de Estados Unidos.",
        "curiosidad": "Es la serpiente de cascabel más grande y avisa agitando su cascabel antes de atacar.",
    },
    "Daboia_russelii": {
        "habitat": "Campos y zonas agrícolas del sur de Asia.",
        "curiosidad": "Es una de las 'cuatro grandes' serpientes de India y causa miles de muertes al año.",
    },
    "Dasyatis_pastinaca": {
        "habitat": "Fondos arenosos del Atlántico oriental y el Mediterráneo.",
        "curiosidad": "Su aguijón caudal serrado inspiró el arma que, según la leyenda, mató a Ulises.",
    },
    "Dendrobates_tinctorius": {
        "habitat": "Selvas tropicales de las Guayanas y el norte de Brasil.",
        "curiosidad": "Obtiene su veneno de los insectos que come; criada en cautividad es inofensiva.",
    },
    "Dendroaspis_polylepis": {
        "habitat": "Sabanas y zonas rocosas del África subsahariana.",
        "curiosidad": "Es la serpiente más rápida del mundo (hasta 20 km/h) y su nombre viene del color negro de su boca.",
    },
    "Enhydrina_schistosa": {
        "habitat": "Aguas costeras y estuarios del Indo-Pacífico.",
        "curiosidad": "Es responsable de la mayoría de mordeduras mortales de serpiente marina a pescadores.",
    },
    "Hadronyche_formidabilis": {
        "habitat": "Bosques húmedos del este de Australia.",
        "curiosidad": "Vive en los árboles y es la mayor de las arañas de embudo, con un veneno muy potente.",
    },
    "Hapalochlaena_maculosa": {
        "habitat": "Pozas de marea y arrecifes del sur de Australia.",
        "curiosidad": "Sus anillos azules brillan al sentirse amenazado; lleva tetrodotoxina suficiente para matar a 26 personas.",
    },
    "Heloderma_suspectum": {
        "habitat": "Desiertos del suroeste de EE. UU. y norte de México.",
        "curiosidad": "Es uno de los pocos lagartos venenosos; de su saliva se desarrolló un fármaco contra la diabetes.",
    },
    "Latrodectus_mactans": {
        "habitat": "Rincones oscuros y leñeras de zonas templadas de América.",
        "curiosidad": "La hembra luce un reloj de arena rojo y a veces se come al macho tras aparearse.",
    },
    "Leiurus_quinquestriatus": {
        "habitat": "Desiertos del norte de África y Oriente Medio.",
        "curiosidad": "Su veneno es de los más caros del mundo y se estudia contra los tumores cerebrales.",
    },
    "Lonomia_obliqua": {
        "habitat": "Bosques del sur de Brasil.",
        "curiosidad": "El roce de sus pelos puede provocar hemorragias internas mortales; es la oruga más venenosa conocida.",
    },
    "Loxosceles_laeta": {
        "habitat": "Interior de viviendas y zonas oscuras de Sudamérica.",
        "curiosidad": "Su mordedura puede causar necrosis (muerte del tejido) alrededor de la herida.",
    },
    "Micrurus_fulvius": {
        "habitat": "Bosques y matorrales del sureste de Estados Unidos.",
        "curiosidad": "Sus colores avisan del peligro: 'rojo con amarillo, mata a un compañero'.",
    },
    "Naja_naja": {
        "habitat": "Campos, selvas y aldeas del subcontinente indio.",
        "curiosidad": "Es la cobra de los encantadores de serpientes y despliega su capucha con un dibujo de 'gafas'.",
    },
    "Naja_nigricollis": {
        "habitat": "Sabanas del África subsahariana.",
        "curiosidad": "Escupe veneno a los ojos con puntería desde más de dos metros para defenderse.",
    },
    "Notechis_scutatus": {
        "habitat": "Humedales y praderas del sur de Australia.",
        "curiosidad": "Debe su nombre a las bandas que recuerdan a un tigre; su veneno es muy neurotóxico.",
    },
    "Nycticebus_coucang": {
        "habitat": "Selvas del sudeste asiático.",
        "curiosidad": "Es de los pocos mamíferos venenosos: segrega toxina por el codo y la lame para envenenar su mordedura.",
    },
    "Ophiophagus_hannah": {
        "habitat": "Selvas y bosques del sur y sudeste de Asia.",
        "curiosidad": "Es la serpiente venenosa más larga del mundo (hasta 5,5 m) y se alimenta de otras serpientes.",
    },
    "Ornithorhynchus_anatinus": {
        "habitat": "Ríos y arroyos del este de Australia.",
        "curiosidad": "El macho tiene espolones venenosos en las patas traseras; es un mamífero que pone huevos.",
    },
    "Oxyuranus_microlepidotus": {
        "habitat": "Llanuras áridas del centro de Australia.",
        "curiosidad": "Tiene el veneno más tóxico de todas las serpientes terrestres del mundo.",
    },
    "Paraponera_clavata": {
        "habitat": "Selvas tropicales de Centroamérica y Sudamérica.",
        "curiosidad": "Su picadura es la más dolorosa entre los insectos, comparable a un disparo (de ahí su nombre, 'hormiga bala').",
    },
    "Phoneutria_nigriventer": {
        "habitat": "Selvas y zonas urbanas de Brasil.",
        "curiosidad": "Es de las arañas más venenosas; deambula por el suelo de noche en vez de tejer telas.",
    },
    "Phyllobates_terribilis": {
        "habitat": "Selvas húmedas del Pacífico colombiano.",
        "curiosidad": "Es el animal más venenoso del mundo por toxina: una sola rana tiene veneno para matar a diez personas.",
    },
    "Phyllomedusa_sauvagii": {
        "habitat": "El Chaco seco de Sudamérica.",
        "curiosidad": "Su piel produce péptidos que se estudian como antibióticos y analgésicos.",
    },
    "Physalia_physalis": {
        "habitat": "Superficie de los océanos cálidos, sobre todo del Atlántico.",
        "curiosidad": "No es una medusa, sino una colonia de organismos; sus tentáculos pueden medir hasta 30 metros.",
    },
    "Pitohui_dichrous": {
        "habitat": "Selvas de Nueva Guinea.",
        "curiosidad": "Es uno de los pocos pájaros venenosos: su piel y plumas contienen la misma toxina que las ranas dardo.",
    },
    "Pterois_volitans": {
        "habitat": "Arrecifes del Indo-Pacífico (e invasor en el Atlántico y el Caribe).",
        "curiosidad": "Sus vistosas espinas son venenosas y, como especie invasora, está arrasando los arrecifes del Caribe.",
    },
    "Salamandra_salamandra": {
        "habitat": "Bosques húmedos y arroyos de Europa, incluida España.",
        "curiosidad": "Segrega veneno (samandarina) por la piel; antiguamente se creía que podía vivir en el fuego.",
    },
    "Scolopendra_cingulata": {
        "habitat": "Zonas secas y pedregosas de la cuenca mediterránea.",
        "curiosidad": "Es el ciempiés más común de Europa e inyecta veneno con sus primeras patas modificadas.",
    },
    "Scolopendra_gigantea": {
        "habitat": "Selvas tropicales del norte de Sudamérica.",
        "curiosidad": "Es el ciempiés más grande del mundo (hasta 30 cm) y caza murciélagos al vuelo.",
    },
    "Steatoda_nobilis": {
        "habitat": "Zonas urbanas de Europa, cada vez más presente en España.",
        "curiosidad": "Se parece a la viuda negra, pero su mordedura, aunque dolorosa, rara vez es grave.",
    },
    "Synanceia_horrida": {
        "habitat": "Fondos costeros del Indo-Pacífico.",
        "curiosidad": "Es el pez más venenoso del mundo y se camufla como una piedra, por lo que se pisa sin verlo.",
    },
    "Takifugu_rubripes": {
        "habitat": "Aguas del Pacífico noroccidental, alrededor de Japón.",
        "curiosidad": "Es el famoso 'fugu' japonés: solo cocineros con licencia pueden prepararlo por su tetrodotoxina mortal.",
    },
    "Varanus_komodoensis": {
        "habitat": "Islas de Indonesia (Komodo y alrededores).",
        "curiosidad": "Es el lagarto más grande del mundo y su veneno impide que la sangre de sus presas coagule.",
    },
    "Vespa_mandarinia": {
        "habitat": "Bosques y montañas del este de Asia.",
        "curiosidad": "Es la avispa más grande del mundo; un grupo puede destruir una colmena de abejas en pocas horas.",
    },
    "Vipera_latastei": {
        "habitat": "Zonas secas y rocosas de la península ibérica y el noroeste de África.",
        "curiosidad": "Se reconoce por el pequeño cuerno en la punta del hocico; es una de las víboras propias de España.",
    },
}


def _norm(s):
    # Normaliza para emparejar aunque difieran guiones bajos/espacios/mayúsculas
    return (s or "").replace("_", " ").strip().lower()


def poblar(apps, schema_editor):
    Animal = apps.get_model("animales", "Animal")
    lookup = {_norm(k): v for k, v in DATOS.items()}
    for a in Animal.objects.all():
        info = lookup.get(_norm(a.nombre_cientifico))
        if info:
            a.habitat = info["habitat"]
            a.curiosidad = info["curiosidad"]
            a.save(update_fields=["habitat", "curiosidad"])


def vaciar(apps, schema_editor):
    Animal = apps.get_model("animales", "Animal")
    Animal.objects.update(habitat=None, curiosidad=None)


class Migration(migrations.Migration):

    dependencies = [
        ("animales", "0001_initial"),
    ]

    operations = [
        migrations.AddField(
            model_name="animal",
            name="habitat",
            field=models.TextField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name="animal",
            name="curiosidad",
            field=models.TextField(blank=True, null=True),
        ),
        migrations.RunPython(poblar, vaciar),
    ]
