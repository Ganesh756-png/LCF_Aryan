# Local Service Finder - Automated Runtime Downloader & Runner
$ProgressPreference = 'SilentlyContinue'

$workDir = "d:\DA_V\HMW"
$jdkDir = "$workDir\.jdk"
$mavenDir = "$workDir\.maven"

Write-Host "=== Local Service Finder Automatic Environment Setup ===" -ForegroundColor Cyan

# Clean up any leftover lock files
$jdkZip = "$workDir\jdk.zip"
$mvnZip = "$workDir\maven.zip"
if (Test-Path $jdkZip) { Remove-Item $jdkZip -Force -ErrorAction SilentlyContinue }
if (Test-Path $mvnZip) { Remove-Item $mvnZip -Force -ErrorAction SilentlyContinue }

# Define JDK 17 Installation Function
function Install-JDK {
    Write-Host "[1/3] Java JDK 17 not found. Downloading JDK 17..." -ForegroundColor Yellow
    if (Test-Path $jdkDir) { Remove-Item $jdkDir -Recurse -Force -ErrorAction SilentlyContinue }
    New-Item -ItemType Directory -Path $jdkDir -Force | Out-Null
    
    $jdkUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.10_7.zip"
    Write-Host "Downloading JDK (180MB)... Please wait..." -ForegroundColor Gray
    Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip
    
    Write-Host "Extracting JDK..." -ForegroundColor Gray
    Expand-Archive -Path $jdkZip -DestinationPath "$jdkDir\temp"
    
    $unzippedDir = Get-ChildItem -Path "$jdkDir\temp" -Directory | Select-Object -First 1
    if ($null -eq $unzippedDir) {
        Write-Host "[ERROR] Failed to extract JDK." -ForegroundColor Red
        exit 1
    }
    
    $fullName = $unzippedDir.FullName
    Write-Host "Moving JDK files from: $fullName" -ForegroundColor Gray
    Move-Item -Path "$fullName\*" -Destination $jdkDir -Force
    Remove-Item "$jdkDir\temp" -Recurse -Force -ErrorAction SilentlyContinue | Out-Null
    Remove-Item $jdkZip -Force -ErrorAction SilentlyContinue | Out-Null
    Write-Host "[OK] JDK 17 Setup Completed." -ForegroundColor Green
}

# Define Maven Installation Function
function Install-Maven {
    Write-Host "[2/3] Maven not found. Downloading Maven..." -ForegroundColor Yellow
    if (Test-Path $mavenDir) { Remove-Item $mavenDir -Recurse -Force -ErrorAction SilentlyContinue }
    New-Item -ItemType Directory -Path $mavenDir -Force | Out-Null
    
    $mvnUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    Write-Host "Downloading Maven..." -ForegroundColor Gray
    Invoke-WebRequest -Uri $mvnUrl -OutFile $mvnZip
    
    Write-Host "Extracting Maven..." -ForegroundColor Gray
    Expand-Archive -Path $mvnZip -DestinationPath "$mavenDir\temp"
    
    $unzippedDir = Get-ChildItem -Path "$mavenDir\temp" -Directory | Select-Object -First 1
    if ($null -eq $unzippedDir) {
        Write-Host "[ERROR] Failed to extract Maven." -ForegroundColor Red
        exit 1
    }
    
    $fullName = $unzippedDir.FullName
    Write-Host "Moving Maven files from: $fullName" -ForegroundColor Gray
    Move-Item -Path "$fullName\*" -Destination $mavenDir -Force
    Remove-Item "$mavenDir\temp" -Recurse -Force -ErrorAction SilentlyContinue | Out-Null
    Remove-Item $mvnZip -Force -ErrorAction SilentlyContinue | Out-Null
    Write-Host "[OK] Maven Setup Completed." -ForegroundColor Green
}

# Run setup checks
$hasJava = Test-Path "$jdkDir\bin\java.exe"
if (-not $hasJava) { Install-JDK } else { Write-Host "[OK] Local JDK 17 detected." -ForegroundColor Green }

$hasMaven = Test-Path "$mavenDir\bin\mvn.cmd"
if (-not $hasMaven) { Install-Maven } else { Write-Host "[OK] Local Maven detected." -ForegroundColor Green }

# Load Environment Variables from .env file if it exists
if (Test-Path "$workDir\.env") {
    Write-Host "Loading environment variables from .env..." -ForegroundColor Magenta
    Get-Content "$workDir\.env" | ForEach-Object {
        $line = $_.Trim()
        # Skip empty lines and comment lines
        if ($line -and -not $line.StartsWith("#")) {
            $pos = $line.IndexOf('=')
            if ($pos -gt 0) {
                $name = $line.Substring(0, $pos).Trim()
                $value = $line.Substring($pos + 1).Trim()
                # Remove surrounding quotes if they exist
                if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
                    $value = $value.Substring(1, $value.Length - 2)
                }
                Set-Item -Path "env:$name" -Value $value
                Write-Host "  Loaded: $name" -ForegroundColor Gray
            }
        }
    }
} else {
    Write-Host "No .env file found. Using default local properties/mock mode." -ForegroundColor Yellow
}

# Configure local Environment Paths
$env:JAVA_HOME = $jdkDir
$env:M2_HOME = $mavenDir
$env:Path = "$jdkDir\bin;$mavenDir\bin;" + $env:Path

# Test Java and Maven execution
Write-Host "Verifying installations:" -ForegroundColor Gray
java -version
mvn -version

Write-Host "[3/3] Launching Local Service Finder Web App..." -ForegroundColor Cyan
Write-Host "Starting Maven build and running application on http://localhost:8080..." -ForegroundColor Gray

# Compile & Run
mvn clean spring-boot:run
