from rest_framework import serializers
from .models import Animal, Consulta

class AnimalSerializer(serializers.ModelSerializer):
    # Definimos el campo de imagen de forma personalizada
    imagen_url = serializers.SerializerMethodField()

    class Meta:
        model = Animal
        # Esto incluirá todos los campos: nombre, toxicidad, descripción, síntomas, etc.
        fields = '__all__'

    def get_imagen_url(self, obj):
        if obj.imagen_url:
            # Pegamos tu IP de AWS para que la App reciba la ruta completa
            # Asegúrate de que la IP 98.90.201.0 es la correcta actualmente
            return f"http://98.90.201.0:8000{obj.imagen_url.url}"
        return None

class ConsultaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Consulta
        fields = '__all__'