from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers import causal_router, data_router

app = FastAPI(title="智慧养殖因果分析服务", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(causal_router.router, prefix="/api/causal", tags=["因果分析"])
app.include_router(data_router.router, prefix="/api/data", tags=["数据处理"])

@app.get("/")
def root():
    return {"message": "智慧养殖因果分析服务"}

@app.get("/health")
def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)