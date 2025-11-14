from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class Section(BaseModel):
    title: str
    content: str
    section_type: str  # e.g., "requirements", "overview", "timeline"
    confidence: float = Field(..., ge=0, le=1)

class ParseRequest(BaseModel):
    job_id: str
    text: str

class ParseResponse(BaseModel):
    job_id: str
    sections: List[Section]
    status: str = "success"
