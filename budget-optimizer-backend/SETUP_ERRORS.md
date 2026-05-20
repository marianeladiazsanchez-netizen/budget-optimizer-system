# Setup del Backend - Solución de Errores

## Problema
Los errores "The import org.springframework cannot be resolved" aparecen porque:
1. Las dependencias Maven no han sido descargadas
2. Java 21 no está configurado correctamente
3. Maven no está en el PATH del sistema

## Solución

### 1. Instalar Java 21
Descarga e instala Java 21 desde:
- [Oracle Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- O [Eclipse Temurin Java 21](https://adoptium.net/)

Después de instalar:
```powershell
# Verifica la instalación
java -version
```

### 2. Instalar Maven (Opcional - Ya está en el proyecto)
El proyecto incluye `mvnw.cmd`, así que Maven wrapper ya está disponible.

### 3. Compilar el Backend
Una vez que Java esté instalado:

```powershell
cd c:\Users\NELA\Desktop\budget-optimizer-system\budget-optimizer-backend

# Compilar y descargar dependencias
.\mvnw.cmd clean compile

# O construir el JAR
.\mvnw.cmd clean package
```

### 4. Alternativa: Usar VS Code Extension
Si prefieres, instala la extensión "Extension Pack for Java" en VS Code:
- Abre VS Code
- Ve a Extensions (Ctrl+Shift+X)
- Busca "Extension Pack for Java"
- Haz clic en Install

VS Code detectará automáticamente el proyecto y compilará las dependencias.

### 5. Una vez compilado
Los errores en rojo desaparecerán y podrás:
- Ejecutar el backend: `.\mvnw.cmd spring-boot:run`
- Ver la API en: `http://localhost:8080`

## Notas
- El backend requiere **Java 21** (especificado en pom.xml)
- Puerto: **8080**
- Base de datos: **PostgreSQL** (configure en `.env`)

## Próximos pasos
Una vez compilado:
1. Configura `.env` con credenciales de PostgreSQL
2. Crea la base de datos
3. Ejecuta las migraciones SQL en `src/main/resources/data.sql`
4. Inicia el backend con Maven
