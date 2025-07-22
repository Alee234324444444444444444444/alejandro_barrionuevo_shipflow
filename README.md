# Alejandro Barrionuevo - Shipflow
# 📦 Shipflow - Sistema de Gestión de Envíos

Sistema de gestión de envíos de paquetes desarrollado con **Spring Boot** y **Kotlin**, que ayuda a registrar y gestionar estados de paquetes.
---

## 📋 **Descripción General**

`Shipflow` es una sistema de envios que nos permite registrar paquetes con diferentes estados siguiendola regla del negocio y registra un historial completo de eventos por lo que ha pasado un paquete.

## 🧱 Arquitectura del backend

### 📂 Estructura del Proyecto Shipflow

El backend sigue una arquitectura por capas con la siguiente estructura:

```plaintext
com/
└── pucetec/
    └── alejandro_barrionuevo_shipflow/
        ├── controllers/
        │   └── PackageController.kt                   # Controlador principal REST
        │
        ├── exceptions/                #EXCEPCIONES PARA LA LOGICA DE NEGOCIO
        │   ├── BusinessRuleException.kt
        │   ├── DescriptionTooLongException.kt
        │   ├── InvalidCityException.kt
        │   ├── InvalidStatusException.kt
        │   ├── InvalidStatusTransitionException.kt
        │   ├── InvalidTypeException.kt
        │   └── PackageNotFoundException.kt
        │
        ├── mappers/
        │   ├── PackageEventMapper.kt
        │   └── PackageMapper.kt
        │
        ├── models/
        │   ├── entities/
        │   │   ├── BaseEntity.kt
        │   │   ├── Package.kt
        │   │   ├── PackageEvent.kt
        │   │   ├── PackageType.kt
        │   │   └── Status.kt
        │   │
        │   ├── requests/
        │   │   ├── PackageRequest.kt
        │   │   └── UpdateStatusRequest.kt
        │   │
        │   └── responses/
        │       ├── PackageDetailResponse.kt
        │       ├── PackageEventResponse.kt
        │       ├── PackageResponse.kt
        │       └── UpdateStatusResponse.kt
        ├── repositories/
        │   ├── PackageEventRepository.kt
        │   └── PackageRepository.kt
        ├── routes/                                      # Si defines rutas personalizadas aquí
        ├── services/
        │   └── PackageService.kt                        # Lógica de negocio
        │
        └── AlejandroBarrionuevoShipflowApplication.kt   # Clase principal de arranque

```

### **Funcionalidades Principales del Proyecto:**

**Registro de Envíos:**
- Tipos de paquete permitidos: `DOCUMENT`, `SMALL_BOX`, `FRAGILE`
- Validación de origen y destino no pueden ser iguales da un error controlado
- Validación de descripción es maximo 50 caracteres
- Tracking ID se genera automáticamente
- Estado inicial: `PENDING`
- Fecha estimada de entrega incluida

**Consulta de Envíos:**
- Lista todos los paquetes registrados
- Consulta por Tracking ID
- Consulta el historial de los cambios de estado del paquete

**Gestión de Estados:**
- Estados posibles: `PENDING`, `IN_TRANSIT`, `DELIVERED`, `ON_HOLD`, `CANCELLED`
- Validación estricta de transiciones según reglas definidas
- Registro histórico de cada cambio de estado con comentario opcional

---

## ⚙️ **Requisitos Previos**

Asegúrate de tener instalado:

- **Java 21**
- Tener **Docker** y **Docker Compose** instalado y en ejecución
- Tener **Git** instalado
- Tener **Dbeaver -> abierto con una conexión a postgresql**

---

## 🛠️ Configuración en DBeaver

1. Abre **DBeaver**.
2. Crea una nueva conexión PostgreSQL.
3. Configura con los siguientes parámetros:

```
Host: localhost
Puerto: 6969
Base de datos: shipflow_db
Usuario: admin
Contraseña: admin
```

## 🚀 **Ejecución del Proyecto**

### **1. Clonar el Repositorio**
```bash
git clone https://github.com/Alee234324444444444444444444/alejandro_barrionuevo_shipflow.git
cd alejandro_barrionuevo_shipflow
```

---

## 🚀 Levantar el proyecto

1. Asegúrate de tener **Docker Desktop** activo.
2. En la raíz del proyecto, ejecuta:

```bash
docker-compose up
```

🧱 Esto levantará el contenedor de PostgreSQL necesario para el backend.

3. Abre otra terminal o IntelliJ IDEA y corre el backend con:

```bash
./gradlew bootRun
```

## 🔗 Endpoints – Shipflow

| ⚙️ Método | 📍 Ruta                                       | 📄 Descripción                         |
| --------- | --------------------------------------------- | -------------------------------------- |
| `POST`    | `/shipflow/api/packages`                      | Crear nuevo paquete                    |
| `GET`     | `/shipflow/api/packages`                      | Obtener todos los paquetes registrados |
| `GET`     | `/shipflow/api/packages/{trackingId}`         | Obtener un paquete por Tracking ID     |
| `GET`     | `/shipflow/api/packages/{trackingId}/history` | Ver historial completo del paquete     |
| `PUT`     | `/shipflow/api/packages/{trackingId}/status`  | Actualizar estado del paquete          |

---

## 🔗 **APIs Endpoints**

**Base URL:** `http://localhost:8080/shipflow/api`

### **📦 Creacion de paquete flujo correcto**

#### **Crear Envío**
```http
POST /shipflow/api/packages

{
  "type": "DOCUMENT",
  "weight": 0.5,
  "description": "Contrato de compraventa",
  "city_from": "Quito",
  "city_to": "Guayaquil"
}
```

#### **Actualizar Estado a IN_TRANSIT**
```http
PUT /shipflow/api/packages/{trackingId}/status

{
  "status": "IN_TRANSIT",
  "comment": "Recogido en oficina"
}
```

#### **Cambiar estado a DELIVERED (válido porque pasó por IN_TRANSIT)**
```http
PUT /shipflow/api/packages/{trackingId}/status

{
  "status": "DELIVERED"
}
```


### **📦 Creacion de paquete con flujo erroneo y verficación de exepciones**

#### **Crear Envío con misma ciudad**
```http
POST /shipflow/api/packages

{
  "type": "SMALL_BOX",
  "weight": 2.0,
  "description": "Papelería",
  "city_from": "Cuenca",
  "city_to": "Cuenca"
}
```

#### **Crear Envío con descripcion mayor a 50 caracteres**
```http
POST /shipflow/api/packages

{
  "type": "FRAGILE",
  "weight": 1.2,
  "description": "Este texto supera el límite de los cincuenta caracteres permitidos",
  "city_from": "Loja",
  "city_to": "Ambato"
}
```

#### **Crear Envío correcto**
```http
POST /shipflow/api/packages

{
  "type": "SMALL_BOX",
  "weight": 2.0,
  "description": "Papelería",
  "city_from": "Cuenca",
  "city_to": "Quito"
}
```

#### **Actualizar Estado no existente**
```http
PUT /shipflow/api/packages/{trackingId}/status

{
  "status": "LOST",
  "comment": "Se perdió"
}
```

#### **Actualizar Estado estando de pending a delivered**
```http
PUT /shipflow/api/packages/{trackingId}/status

{
  "status": "DELIVERED"
}
```










