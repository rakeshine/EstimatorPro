import json
from pathlib import Path
import html

OUTPUT_HTML = "output_infographic.html"


def format_cell(value: str) -> str:
    """Render a cell's text as HTML. Supports simple bullet lists via lines starting with '- '."""
    if value is None:
        return ""

    # Normalize line breaks
    text = str(value).replace("<br/>", "<br>")

    # If any line starts with '- ', render as a bullet list
    lines = [ln.strip() for ln in text.split("\n")]
    if any(ln.startswith("- ") for ln in lines):
        items = []
        for ln in lines:
            if ln.startswith("- "):
                items.append(f"<li>{html.escape(ln[2:].strip())}</li>")
            elif ln:
                items.append(f"<li>{html.escape(ln)}</li>")
        return "<ul>" + "".join(items) + "</ul>"

    # Otherwise, allow <br> as explicit line breaks, escape everything else
    parts = [html.escape(p) for p in text.split("<br>")]
    return "<br>".join(parts)


def generate_html(input_json, output_path: str = OUTPUT_HTML):
    # Input schema:
    # {
    #   "columns": {"design1": "Fixed Goal Agent", ...},
    #   "rows": {
    #       "description": {"label": "Description", "design1": "..." ...},
    #       ...
    #   }
    # }

    columns = input_json["columns"]
    rows = input_json["rows"]

    # Preserve design column order explicitly
    design_order = ["design1", "design2", "design3", "design4"]
    agents = [columns[k] for k in design_order]

    # Map agent display names to their design keys for lookup
    agent_key_by_name = {columns[k]: k for k in design_order}

    icon_map = {
        "Fixed Goal Agent": ("🔒", "#e6f0ff", "#eef3fb"),
        "Adaptive Goal Agent": ("🔄", "#fff7e6", "#fff7ee"),
        "Multi-Skill Agent": ("🧠", "#eefdf6", "#f0fff6"),
        "Specialized Skill Agent": ("🎯", "#fff0f6", "#fff4f8"),
    }

    def cell(row_key: str, agent_name: str) -> str:
        design_key = agent_key_by_name.get(agent_name)
        if not design_key:
            return ""
        return format_cell(rows.get(row_key, {}).get(design_key, ""))

    # HTML skeleton styled to resemble the SVG layout but responsive
    head_html = """
<!doctype html>
<html>
  <head>
    <meta charset=\"utf-8\" />
    <title>Agent Design Comparison</title>
    <style>
      :root { --bg:#f5f7fb; --panel:#fff; --border:#e6eef8; --text:#0f172a; --muted:#334155; }
      html, body { height:100%; }
      body { background: var(--bg); margin:0; font-family: 'Segoe UI', Roboto, Helvetica, Arial, 'Apple Color Emoji', 'Segoe UI Emoji'; color: var(--text); }
      .wrap { display:flex; align-items:flex-start; justify-content:center; padding:40px; }
      .container { background: linear-gradient(90deg, #eef2ff 0%, #ffffff 100%); border-radius:12px; box-shadow: 0 8px 30px rgba(20,30,60,0.08); padding: 24px; max-width: 1200px; width:100%; }
      .header { margin: 8px 8px 20px 8px; }
      .title { font-weight:700; font-size:28px; line-height:1.1; color:#0f172a; }
      .subtitle { font-weight:600; font-size:14px; color:#334155; margin-top:8px; }
      .grid { display:grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 24px; margin-top: 24px; }
      .card { background:#fff; border:1px solid var(--border); border-radius:12px; padding:16px 16px 20px 16px; }
      .icon-row { display:flex; align-items:center; gap:12px; }
      .icon-circle { width:56px; height:56px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-weight:700; }
      .panel-title { font-weight:700; font-size:18px; color:#0b1220; }
      .desc { font-weight:400; font-size:13px; color:#213547; margin-top:10px; }
      .divider { height:1px; margin: 14px 0; background: var(--border); }
      .subtitle-sm { font-weight:600; font-size:13px; color:#0b1220; margin-top:6px; }
      .bullet ul { margin:6px 0 0 0; padding-left:18px; }
      .bullet li { font-size:12px; color:#475569; margin:4px 0; }
      .bullet { font-size:12px; color:#475569; margin:4px 0; }
      .bottom { font-weight:400; font-size:13px; color:#213547; margin-top:10px; }
      @media (max-width: 1200px) { .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
      @media (max-width: 700px) { .grid { grid-template-columns: 1fr; } }
    </style>
  </head>
  <body>
    <div class=\"wrap\">
      <div class=\"container\">
        <div class=\"grid\">
    """

    # Build cards
    cards_html = []
    for agent in agents:
        icon, circle, divider = icon_map.get(agent, ("", "#eef2ff", "#eef2ff"))
        cards_html.append(
            """
          <div class=\"card\">
            <div class=\"icon-row\">
              <div class=\"icon-circle\" style=\"background:%s;\">%s</div>
              <div class=\"panel-title\">%s</div>
            </div>
            <div class=\"desc\">%s</div>
            <div class=\"divider\" style=\"background:%s\"></div>
            <div class=\"subtitle-sm\">Why it matters?</div>
            <div class=\"bullet\">%s</div>
            <div class=\"divider\" style=\"background:%s\"></div>
            <div class=\"subtitle-sm\">How to decide?</div>
            <div class=\"bullet\">%s</div>
            <div class=\"divider\" style=\"background:%s\"></div>
            <div class=\"subtitle-sm\">What to do?</div>
            <div class=\"bullet\">%s</div>
            <div class=\"divider\" style=\"background:%s\"></div>
            <div class=\"subtitle-sm\">What to avoid?</div>
            <div class=\"bullet\">%s</div>
            <div class=\"divider\" style=\"background:%s\"></div>
            <div class=\"subtitle-sm\">Starter</div>
            <div class=\"bullet\">%s</div>
            <div class=\"divider\" style=\"background:%s\"></div>
            <div class=\"subtitle-sm\">Tools</div>
            <div class=\"bullet\">%s</div>
            <div class=\"divider\" style=\"background:%s\"></div>
            <div class=\"subtitle-sm\">Bottom line</div>
            <div class=\"bottom\">%s</div>
          </div>
            """ % (
                circle,
                icon,
                html.escape(agent),
                cell("description", agent),
                divider,
                cell("why_matters", agent),
                divider,
                cell("how_decide", agent),
                divider,
                cell("what_do", agent),
                divider,
                cell("what_avoid", agent),
                divider,
                cell("default_starter", agent),
                divider,
                cell("tools", agent),
                divider,
                cell("bottom_line", agent),
            )
        )

    tail_html = """
        </div>
      </div>
    </div>
  </body>
</html>
    """

    html_out = head_html + "".join(cards_html) + tail_html
    Path(output_path).write_text(html_out, encoding="utf-8")
    print("HTML generated →", output_path)


if __name__ == "__main__":
    with open("input.json", "r", encoding="utf-8") as f:
        table_json = json.load(f)

    generate_html(table_json)
