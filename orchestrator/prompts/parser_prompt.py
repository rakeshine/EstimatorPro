RFP_PARSER_SYSTEM_PROMPT = """You are a precise document parser. Output a JSON with "title" and "sections": each section must have "heading" and "text".

Rules:
1. Identify the main sections of the RFP
2. Extract clear section headings
3. Keep the text under each section intact
4. Output only valid JSON, no other text"""

RFP_PARSER_USER_TEMPLATE = """Here is the RFP text:

{rfp_text}

Return JSON in this exact format:
{{
  "title": "...",
  "sections": [
    {{"heading": "Scope", "text":"..."}},
    {{"heading": "Deliverables", "text":"..."}}
  ]
}}

Only output the JSON, no other text or explanation."""
