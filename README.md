# PaperHUB

PaperHUB is a Spring Boot application for working with PDFs through a simple web interface and REST API. It combines core PDF utilities like merging, compressing, and page-to-image export with AI-powered study helpers such as summarization, quiz generation, and mind map creation.

## Features

- Upload one or more PDF files through the browser or API
- Merge multiple PDFs into a single output file
- Compress a PDF and save a new generated version
- Convert PDF pages into `png` or `jpeg` images packed in a ZIP archive
- Generate AI summaries, quizzes, and hierarchical mind maps from PDF text
- Download generated files from the outputs directory

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
- Maven 3.9+ recommended
- Gemini API key if you want live AI generation

## Getting Started

### 1. Clone and enter the project

```bash
git clone <your-repo-url>
cd PaperHUB
```

### 2. Set the Gemini API key

On macOS or Linux:

```bash
export GEMINI_API_KEY=your_api_key_here
```

If `GEMINI_API_KEY` is not set, AI endpoints still respond, but they return a placeholder message instead of a real Gemini-generated result.

### 3. Run the app

```bash
./mvnw spring-boot:run
```

If the Maven wrapper is not present in your copy, use:

```bash
mvn spring-boot:run
```

The app starts on:

```text
http://localhost:8080
```

Open the browser to that URL to use the built-in interface.

## Configuration

Current defaults from `application.yml`:

```yaml
server:
  port: 8080

paperhub:
  storage:
    uploads-dir: uploads
    outputs-dir: outputs

gemini:
  api-key: ${GEMINI_API_KEY:}
  model: gemini-2.5-pro
  endpoint: https://generativelanguage.googleapis.com/v1beta/models
```

You can customize storage folders, port, and Gemini model through `src/main/resources/application.yml`.

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

- Uploaded files are stored in the local `uploads/` directory.
- Generated files are stored in the local `outputs/` directory.
- File names are made unique automatically to avoid overwriting existing uploads.
- The current compression flow rewrites the PDF and may not always produce a dramatically smaller file depending on the source content.
- AI text extraction uses PDFBox and sends only a trimmed portion of the PDF text to Gemini.

## Development Ideas

- Add automated tests for controllers and services
- Add file type validation and stronger error handling
- Improve compression strategy for image-heavy PDFs
- Support DOCX or image OCR pipelines
- Add authentication and per-user file isolation
