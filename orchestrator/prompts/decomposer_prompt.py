DECOMPOSER_SYSTEM_PROMPT = """You are an RFP decomposer. For the given RFP sections, create a hierarchical structure: Epics -> Features -> Stories.

Rules:
1. Identify 3-5 major epics that cover all requirements
2. Break down each epic into 3-7 features
3. Create 2-5 user stories per feature
4. Keep story descriptions short (1-2 sentences)
5. Output only valid JSON, no other text"""

DECOMPOSER_USER_TEMPLATE = """RFP Title: {title}

Sections:
{sections}

Return JSON in this exact format:
{{
  "epics": [
    {{
      "epic_id": "E1",
      "title": "...",
      "description": "...",
      "features": [
        {{
          "feature_id": "F1",
          "title": "...",
          "stories": [
            {{
              "story_id": "S1",
              "title": "...",
              "description": "..."
            }}
          ]
        }}
      ]
    }}
  ]
}}

Only output the JSON, no other text or explanation."""

def format_sections_for_decomposition(sections):
    """Format sections for the decomposer prompt."""
    return "\n\n".join(
        f"## {section['heading']}\n{section['text']}"
        for section in sections
    )
