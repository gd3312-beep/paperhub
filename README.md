# PaperHUB

PaperHUB is a Spring Boot app for working with PDFs in the browser. It handles everyday PDF jobs like merging, compressing, and page-to-image export, then adds Gemini-powered study tools for summaries, quizzes, and mind maps.

## Features

- Upload one or more PDF files through the browser or API
- Merge multiple PDFs into a single output file
- Compress a PDF and save a new generated version
- Convert PDF pages into `png` or `jpeg` images packed in a ZIP archive
- Generate AI summaries, quizzes, and hierarchical mind maps from PDF text
- Download generated files from the outputs directory
- Reject non-PDF uploads and unsafe filenames

## Tech Stack

- Java 17
- Spring Boot 3.3.3
- Maven
- Apache PDFBox
- Spring WebFlux `WebClient` for Gemini API calls
- Static HTML frontend served from Spring Boot

## Project Structure

```text
src/main/java/com/paperhub
├── config/           # CORS configuration
├── controller/       # REST API endpoints
├── dto/              # API response wrapper
└── service/          # PDF, storage, and AI services

src/main/resources
├── application.yml   # App configuration
└── static/index.html # Frontend UI

uploads/              # Uploaded source files
outputs/              # Generated files available for download
```

## Requirements

- Java 17 or newer
- Maven 3.9 or newer
- Gemini API key if you want live AI generation

## Getting Started

### 1. Clone and enter the project

```bash
git clone <your-repo-url>
cd paperhub
```

### 2. Set the Gemini API key

On macOS or Linux:

```bash
export GEMINI_API_KEY=your_api_key_here
```

On Windows PowerShell:

```powershell
$env:GEMINI_API_KEY="your_api_key_here"
```

If `GEMINI_API_KEY` is not set, AI endpoints still respond, but they return a placeholder message instead of a real Gemini-generated result.

### 3. Install dependencies and run

```bash
mvn spring-boot:run
```

Maven downloads the Java dependencies on the first run.

### 4. Open the app

```text
http://localhost:8080
```

## Build

```bash
mvn clean package
```

The JAR is generated under `target/`, which is ignored by git.

## Configuration

Current defaults from `application.yml`:

```yaml
server:
  port: 8080

paperhub:
  storage:
    uploads-dir: uploads
    outputs-dir: outputs
  cors:
    allowed-origins: http://localhost:8080,http://127.0.0.1:8080

gemini:
  api-key: ${GEMINI_API_KEY:}
  model: gemini-2.5-pro
  endpoint: https://generativelanguage.googleapis.com/v1beta/models
```

You can customize storage folders, port, and Gemini model through `src/main/resources/application.yml`.

To allow a separate frontend during local development, add its origin to `paperhub.cors.allowed-origins`.

## API Overview

All responses use a common wrapper shape similar to:

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

### Upload PDFs

`POST /api/pdf/upload`

Multipart form field:

- `files`: one or more files

Example:

```bash
curl -X POST http://localhost:8080/api/pdf/upload \
  -F "files=@sample1.pdf" \
  -F "files=@sample2.pdf"
```

### Merge PDFs

`POST /api/pdf/merge`

```json
{
  "filenames": ["sample1.pdf", "sample2.pdf"]
}
```

### Compress a PDF

`POST /api/pdf/compress`

```json
{
  "filename": "sample1.pdf"
}
```

### Convert PDF to images

`POST /api/pdf/to-image`

```json
{
  "filename": "sample1.pdf",
  "format": "png"
}
```

Supported formats:

- `png`
- `jpeg`

### AI Summary

`POST /api/ai/summarize`

```json
{
  "filename": "sample1.pdf"
}
```

### AI Quiz

`POST /api/ai/quiz`

```json
{
  "filename": "sample1.pdf"
}
```

### AI Mind Map

`POST /api/ai/mindmap`

```json
{
  "filename": "sample1.pdf"
}
```

### List generated outputs

`GET /api/outputs`

### Download a generated file

`GET /api/download/{filename}`

Example:

```bash
curl -O http://localhost:8080/api/download/merged.pdf
```

## Notes

- Uploaded files are stored in the local `uploads/` directory, which is ignored by git.
- Generated files are stored in the local `outputs/` directory, which is ignored by git.
- File names are made unique automatically to avoid overwriting existing files.
- The app only accepts readable PDF uploads.
- The current compression flow rewrites the PDF and may not always produce a dramatically smaller file depending on the source content.
- AI text extraction uses PDFBox and sends only a trimmed portion of the PDF text to Gemini.

## Development Ideas

- Add automated tests for controllers and services
- Improve compression strategy for image-heavy PDFs
- Support DOCX or image OCR pipelines
- Add authentication and per-user file isolation
