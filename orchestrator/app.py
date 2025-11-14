from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from typing import Dict, Any
import json

app = FastAPI(title="RFP Estimator API")

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/api/estimate")
async def estimate_rfp(file: UploadFile = File(...)) -> Dict[str, Any]:
    """Endpoint to process RFP and return estimation."""
    if not file.filename.endswith('.txt'):
        raise HTTPException(status_code=400, detail="Only .txt files are accepted")
    
    content = await file.read()
    # TODO: Process RFP using the agents
    return {"status": "processing", "message": "Estimation in progress"}

@app.get("/health")
async def health_check() -> Dict[str, str]:
    return {"status": "ok"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
