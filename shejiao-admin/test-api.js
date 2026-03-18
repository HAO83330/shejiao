// 测试API返回的数据结构
import { getCommentByNid } from "./src/api/web/comment";

// 测试特定笔记ID的评论
const nid = "46"; // 蜡笔小新的笔记ID
getCommentByNid(nid).then(response => {
  console.log("API Response:", response);
  console.log("Data:", response.data);
  if (response.data && response.data.length > 0) {
    console.log("First comment:", response.data[0]);
  }
}).catch(error => {
  console.error("Error:", error);
});
