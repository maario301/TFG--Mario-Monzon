from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.back_office_url if hasattr(admin.site, 'back_office_url') else admin.site.urls), # O simplemente admin.site.urls
    path('admin/', admin.site.urls),
    path('api/', include('animales.urls')), # <--- Esta es la clave
]