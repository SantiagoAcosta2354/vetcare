# Arranca un servicio de VetCare con su entorno local listo.
#
#   .\arrancar-vetcare.ps1                 -> vetcare-usuarios (8081)
#   .\arrancar-vetcare.ps1 pacientes       -> vetcare-pacientes (8082)
#
# Se encarga de tres cosas que suelen fallar a mano: definir el secreto
# compartido, verificar que MongoDB responda y ejecutar el servicio en su carpeta.

param(
    [string]$Servicio = "usuarios"
)

$ErrorActionPreference = "Stop"
$raiz = $PSScriptRoot
$modulo = "vetcare-$Servicio"
$carpeta = Join-Path $raiz $modulo

if (-not (Test-Path (Join-Path $carpeta "pom.xml"))) {
    Write-Host "No existe el modulo $modulo en $raiz." -ForegroundColor Red
    exit 1
}

# El secreto es compartido: si cada servicio usara uno distinto, todo daria 401.
if (-not $env:VETCARE_JWT_SECRET) {
    $env:VETCARE_JWT_SECRET = "vetcare-clave-de-desarrollo-cambiar-en-produccion-2026"
    Write-Host "Secreto JWT de desarrollo definido para esta sesion." -ForegroundColor Yellow
}

# MongoDB local
$mongoArriba = (Test-NetConnection -ComputerName localhost -Port 27017 -WarningAction SilentlyContinue).TcpTestSucceeded
if (-not $mongoArriba) {
    Write-Host "MongoDB no responde en localhost:27017. Intentando levantar el contenedor..." -ForegroundColor Yellow
    $dockerActivo = $false
    try {
        docker info --format '{{.ServerVersion}}' 2>$null | Out-Null
        $dockerActivo = $LASTEXITCODE -eq 0
    } catch {
        $dockerActivo = $false
    }

    if (-not $dockerActivo) {
        Write-Host "Docker Desktop no esta corriendo." -ForegroundColor Red
        Write-Host "Abra Docker Desktop y vuelva a intentar, o defina VETCARE_MONGO_URI con su cadena de MongoDB Atlas." -ForegroundColor Red
        exit 1
    }

    docker compose -f (Join-Path $raiz "docker-compose.yml") up -d mongo
    for ($i = 0; $i -lt 20; $i++) {
        Start-Sleep -Seconds 3
        if ((Test-NetConnection -ComputerName localhost -Port 27017 -WarningAction SilentlyContinue).TcpTestSucceeded) {
            $mongoArriba = $true
            break
        }
    }
    if (-not $mongoArriba) {
        Write-Host "El contenedor arranco pero MongoDB no responde. Revise 'docker logs vetcare-mongo'." -ForegroundColor Red
        exit 1
    }
    Write-Host "MongoDB listo en localhost:27017." -ForegroundColor Green
}

Write-Host "Arrancando $modulo..." -ForegroundColor Cyan
Push-Location $carpeta
try {
    mvn spring-boot:run
} finally {
    Pop-Location
}
