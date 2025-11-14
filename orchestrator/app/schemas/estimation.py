from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class LineItem(BaseModel):
    story_id: str
    title: str
    size: str
    roles: List[Dict[str, Any]]
    total_hours: float
    total_cost: float

class EstimateRequest(BaseModel):
    job_id: str
    sized_stories: List[Dict[str, Any]]
    ratecard: Dict[str, float]  # role -> hourly_rate

class EstimateResponse(BaseModel):
    job_id: str
    line_items: List[LineItem]
    total_hours: float
    total_cost: float
    confidence: float = Field(..., ge=0, le=1)
    status: str = "success"
