#!/bin/bash

# 对话功能测试脚本
# 使用前请确保服务已启动：http://localhost:9001

BASE_URL="http://localhost:9001/chat"

echo "======================================"
echo "对话功能测试"
echo "======================================"
echo ""

# 测试 1: 简单问候（应该触发 Direct）
echo "测试 1: 简单问候"
echo "请求：你好"
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"text": "你好"}' | jq '.'
echo ""
echo "---"
echo ""

# 测试 2: 逻辑推理问题（应该触发 CoT）
echo "测试 2: 逻辑推理问题"
echo "请求：请证明勾股定理"
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"text": "请证明勾股定理"}' | jq '.'
echo ""
echo "---"
echo ""

# 测试 3: 多方案决策（应该触发 ToT）
echo "测试 3: 多方案决策"
echo "请求：我想学习编程，应该选择 Python 还是 Java？"
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"text": "我想学习编程，应该选择 Python 还是 Java？"}' | jq '.'
echo ""
echo "---"
echo ""

# 测试 4: GET 方式
echo "测试 4: GET 方式"
echo "请求：你是谁？"
curl -s -G "$BASE_URL" \
  --data-urlencode "text=你是谁？" | jq '.'
echo ""
echo "---"
echo ""

# 测试 5: 流式输出（仅显示前 5 行）
echo "测试 5: 流式输出（前 5 行）"
echo "请求：讲一个故事"
curl -s "$BASE_URL/stream?text=讲一个故事" | head -n 5
echo ""
echo "---"
echo ""

echo "测试完成！"
