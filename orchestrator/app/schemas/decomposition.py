from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class Story(BaseModel):
    story_id: str
    title: str
    description: str
    acceptance_criteria: List[str] = []
    priority: str = "medium"  # high, medium, low

class Feature(BaseModel):
    feature_id: str
    title: str
    description: str
    stories: List[Story] = []

class Epic(BaseModel):
    epic_id: str
    title: str
    description: str
    features: List[Feature] = []

class DecomposeRequest(BaseModel):
    job_id: str
    sections: List[Dict[str, Any]]

class DecomposeResponse(BaseModel):
    job_id: str
    epics: List[Epic]
    status: str = "success"
