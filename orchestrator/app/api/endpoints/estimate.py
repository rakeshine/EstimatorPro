from fastapi import APIRouter, HTTPException
import logging
from typing import Dict, Any, List

from app.schemas.estimation import EstimateRequest, EstimateResponse, LineItem

router = APIRouter()
logger = logging.getLogger(__name__)

@router.post("", response_model=EstimateResponse)
async def create_estimate(request: EstimateRequest) -> Dict[str, Any]:
    """
    Generate cost estimates based on sized stories and rate card.
    
    This implementation calculates the total cost based on the hours
    for each role and their respective rates from the rate card.
    """
    try:
        line_items: List[LineItem] = []
        total_hours = 0.0
        total_cost = 0.0
        
        for story in request.sized_stories:
            story_hours = 0.0
            story_cost = 0.0
            role_details = []
            
            # Calculate cost for each role in the story
            for role_est in story.get("roles", []):
                role_name = role_est["role"]
                role_hours = role_est["hours"]
                role_rate = request.ratecard.get(role_name, 0)  # Default to 0 if role not in rate card
                role_cost = role_hours * role_rate
                
                story_hours += role_hours
                story_cost += role_cost
                
                role_details.append({
                    "role": role_name,
                    "hours": role_hours,
                    "rate": role_rate,
                    "cost": role_cost
                })
            
            total_hours += story_hours
            total_cost += story_cost
            
            line_items.append(
                LineItem(
                    story_id=story.get("story_id", ""),
                    title=story.get("title", ""),
                    size=story.get("size", "M"),
                    roles=role_details,
                    total_hours=story_hours,
                    total_cost=story_cost
                )
            )
        
        # Calculate confidence based on the quality of estimates
        # This is a simplified example - in reality, this would consider more factors
        confidence = min(0.9, 0.7 + (len(line_items) * 0.02))  # More stories = higher confidence
        
        return EstimateResponse(
            job_id=request.job_id,
            line_items=line_items,
            total_hours=total_hours,
            total_cost=total_cost,
            confidence=min(confidence, 0.95)  # Cap at 95%
        )
        
    except Exception as e:
        logger.error(f"Error generating estimate: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Error generating estimate: {str(e)}")
