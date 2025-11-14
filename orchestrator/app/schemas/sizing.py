from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class RoleEstimate(BaseModel):
    role: str
    hours: float

class SizedStory(BaseModel):
    story_id: str
    title: str
    description: str
    size: str  # XS, S, M, L, XL
    size_notes: str
    estimated_hours: float
    roles: List[RoleEstimate]
    confidence: float = Field(..., ge=0, le=1)

class SizeRequest(BaseModel):
    job_id: str
    stories: List[Dict[str, Any]]

class SizeResponse(BaseModel):
    job_id: str
    stories_with_size: List[SizedStory]
    status: str = "success"
