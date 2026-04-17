from django.contrib import admin
from .models import Animal, Consulta

@admin.register(Animal)
class AnimalAdmin(admin.ModelAdmin):
    list_display = ('nombre_comun', 'nombre_cientifico', 'toxicidad')
    search_fields = ('nombre_comun', 'nombre_cientifico')

@admin.register(Consulta)
class ConsultaAdmin(admin.ModelAdmin):
    list_display = ('animal', 'fecha', 'probabilidad')
    list_filter = ('fecha',)