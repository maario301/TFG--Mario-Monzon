from django.db import models
from django.contrib.auth.models import User # Importamos los usuarios de Django

class Animal(models.Model):
    nombre_comun = models.CharField(max_length=100)
    nombre_cientifico = models.CharField(max_length=100, unique=True)
    descripcion = models.TextField()
    
    toxicidad = models.CharField(max_length=50) 
    sintomas = models.TextField()
    tratamiento = models.TextField()
    
    imagen_url = models.URLField(max_length=500, blank=True, null=True)

    def __str__(self):
        return self.nombre_comun

class Consulta(models.Model):
    # Relacionamos la consulta directamente con el usuario logueado en la App
    usuario = models.ForeignKey(User, on_delete=models.CASCADE) 
    animal = models.ForeignKey(Animal, on_delete=models.CASCADE)
    fecha = models.DateTimeField(auto_now_add=True)
    probabilidad = models.FloatField(default=0.0)
    
    # Campos vitales para el Paso 5: El Mapa
    latitud = models.FloatField(null=True, blank=True)
    longitud = models.FloatField(null=True, blank=True)

    def __str__(self):
        return f"{self.usuario.username} vio un {self.animal.nombre_comun} el {self.fecha}"