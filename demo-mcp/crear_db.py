"""Crea demo-mcp/banco.db (SQLite) con el mismo esquema y datos sintéticos que usa la app en H2.

Uso: python demo-mcp/crear_db.py
"""
import pathlib
import sqlite3

raiz = pathlib.Path(__file__).resolve().parent.parent
recursos = raiz / "src" / "main" / "resources"
db = raiz / "demo-mcp" / "banco.db"
db.unlink(missing_ok=True)

con = sqlite3.connect(db)
for archivo in ("schema.sql", "data.sql"):
    con.executescript((recursos / archivo).read_text(encoding="utf-8"))
con.commit()
tablas = [r[0] for r in con.execute("SELECT name FROM sqlite_master WHERE type='table'")]
con.close()
print(f"{db} creada con tablas: {', '.join(tablas)}")
