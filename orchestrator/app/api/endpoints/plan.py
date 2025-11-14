from fastapi import APIRouter, HTTPException
import logging
from typing import Dict, Any, List
from datetime import datetime, timedelta

from app.schemas.planning import PlanRequest, PlanResponse, Milestone

router = APIRouter()
logger = logging.getLogger(__name__)

@router.post("", response_model=PlanResponse)
async def create_plan(request: PlanRequest) -> Dict[str, Any]:
    """
    Generate a project plan with a Gantt chart and milestones.
    
    This implementation creates a simple timeline based on the estimate
    and the specified sprint length.
    """
    try:
        # Extract total hours from the estimate
        total_hours = request.estimate.get("total_hours", 0)
        
        # Calculate team capacity (in hours per sprint)
        # This is a simplified calculation - in reality, you'd consider team size and availability
        team_capacity = 5 * 8 * request.sprint_length_days  # 5 devs * 8 hours/day * sprint length
        
        # Calculate number of sprints needed
        num_sprints = max(1, round(total_hours / team_capacity))
        
        # Generate milestones
        today = datetime.now().date()
        milestones: List[Milestone] = []
        
        for i in range(1, num_sprints + 1):
            milestone_date = today + timedelta(days=i * request.sprint_length_days)
            milestones.append(
                Milestone(
                    name=f"Sprint {i} Complete",
                    date=milestone_date.isoformat(),
                    description=f"Completion of Sprint {i} deliverables",
                    type="sprint_end"
                )
            )
        
        # Add final delivery milestone
        final_date = today + timedelta(days=num_sprints * request.sprint_length_days)
        milestones.append(
            Milestone(
                name="Project Delivery",
                date=final_date.isoformat(),
                description="Final delivery of all features",
                type="release"
            )
        )
        
        # Generate Mermaid Gantt chart
        gantt = generate_gantt_chart(milestones, request.sprint_length_days)
        
        return PlanResponse(
            job_id=request.job_id,
            gantt_mermaid=gantt,
            milestones=milestones
        )
        
    except Exception as e:
        logger.error(f"Error generating project plan: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Error generating project plan: {str(e)}")

def generate_gantt_chart(milestones: List[Milestone], sprint_length_days: int) -> str:
    """Generate a Mermaid Gantt chart from milestones."""
    today = datetime.now().date()
    
    # Start building the Mermaid Gantt chart
    gantt = """gantt
    title Project Timeline
    dateFormat  YYYY-MM-DD
    axisFormat %b %d
    
    section Sprints
    """
    
    # Add sprints
    for i, milestone in enumerate(milestones[:-1], 1):  # Exclude the final delivery milestone
        start_date = today + timedelta(days=(i-1) * sprint_length_days)
        end_date = today + timedelta(days=i * sprint_length_days)
        gantt += f'    Sprint {i} :a{i}, {start_date.isoformat()}, {sprint_length_days}d\n'
    # Add milestones
    gantt += "\n    section Milestones\n"
    for i, milestone in enumerate(milestones, 1):
        gantt += f'    {milestone.name} :milestone, m{i}, {milestone.date}, 0d\n'
    return gantt
