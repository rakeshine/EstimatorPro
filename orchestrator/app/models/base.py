from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, String, DateTime, func
import uuid

Base = declarative_base()

def generate_uuid():
    return str(uuid.uuid4())

class BaseModel(Base):
    __abstract__ = True
    
    id = Column(String, primary_key=True, default=generate_uuid)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())
