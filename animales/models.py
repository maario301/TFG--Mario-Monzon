from django.db import models

class Animal(models.Model):
    nombre_comun = models.CharField(max_length=100)
    nombre_cientifico = models.CharField(max_length=100) # Ya sin el italic
    descripcion = models.TextField()
    
    toxicidad = models.CharField(max_length=50) 
    sintomas = models.TextField()
    tratamiento = models.TextField()
    
    imagen_url = models.URLField(max_length=500, blank=True, null=True)

    def __str__(self):
        return self.nombre_comun

class Consulta(models.Model):
    firebase_uid = models.CharField(max_length=128)
    animal = models.ForeignKey(Animal, on_delete=models.CASCADE)
    fecha = models.DateTimeField(auto_now_add=True)
    probabilidad = models.FloatField(default=0.0)

    def __str__(self):
        return f"Consulta de {self.animal.nombre_comun} - {self.fecha}"