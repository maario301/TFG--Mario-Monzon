from rest_framework import serializers
from .models import Animal, Consulta

# animales/serializers.py
class AnimalSerializer(serializers.ModelSerializer):
    imagen_url = serializers.SerializerMethodField()

    class Meta:
        model = Animal
        fields = '__all__'

    def get_imagen_url(self, obj):
        if obj.imagen_url:
            # Quitamos el ".url" porque tu campo ya es un texto directamente
            url_texto = str(obj.imagen_url)
            
            # Si el texto ya empieza por /media/, lo pegamos a la IP
            if url_texto.startswith('/'):
                return f"http://98.90.201.0:8000{url_texto}"
            else:
                return f"http://98.90.201.0:8000/{url_texto}"
        return None
        return None

class ConsultaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Consulta
        fields = '__all__'