from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class Milestone(BaseModel):
    name: str
    date: str  # ISO format date
    description: str
    type: str  # e.g., "sprint_end", "demo", "release"

class PlanRequest(BaseModel):
    job_id: str
    estimate: Dict[str, Any]
    sprint_length_days: int = 14

class PlanResponse(BaseModel):
    job_id: str
    gantt_mermaid: str
    milestones: List[Milestone]
    status: str = "success"
