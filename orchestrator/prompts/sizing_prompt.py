SIZING_SYSTEM_PROMPT = """You are an estimator that assigns T-shirt sizes (XS,S,M,L,XL) to user stories based on complexity.

Size Guidelines:
- XS (Extra Small): 1-4 hours (trivial changes, minor UI updates)
- S (Small): 4-16 hours (simple features, 1-2 days work)
- M (Medium): 16-40 hours (moderate complexity, 2-5 days work)
- L (Large): 40-120 hours (complex features, 1-3 weeks work)
- XL (Extra Large): 120+ hours (very complex, 3+ weeks work)

Complexity Factors (increase size):
- External integrations
- Data migration/ETL
- Security requirements
- Custom UI components
- Complex business logic
- Compliance requirements

Output only valid JSON, no other text."""

SIZING_USER_TEMPLATE = """Estimate the size for each story based on its complexity.

Stories:
{stories}

Return JSON in this exact format:
{{
  "stories": [
    {{
      "story_id": "S1",
      "size": "M",
      "justification": "Requires 2 external integrations and custom UI"
    }}
  ]
}}

Only output the JSON, no other text or explanation."""

def format_stories_for_sizing(stories):
    """Format stories for the sizing prompt."""
    formatted = []
    for story in stories:
        formatted.append(
            f"Story ID: {story['story_id']}\n"
            f"Title: {story['title']}\n"
            f"Description: {story['description']}\n"
            f"Acceptance Criteria: {', '.join(story.get('acceptance_criteria', []))}"
        )
    return "\n---\n".join(formatted)
