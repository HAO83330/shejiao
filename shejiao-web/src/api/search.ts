import request from "@/utils/request";
import { NoteDTO } from "@/type/note"

/**
 * 
 * @param currentPage 
 * @param pageSize 
 * @returns 
 */
export const getRecommendNote = (currentPage: number, pageSize: number) => {
  return request<any>({
    url: `/web/es/note/getRecommendNote/${currentPage}/${pageSize}`, // mock接口
    method: "get",
  });
};

/**
 * 批量获取推荐笔记（用于前端缓存）
 * 一次返回100条推荐数据
 */
export const getRecommendNoteBatch = () => {
  return request<any>({
    url: `/web/es/note/getRecommendNoteBatch`,
    method: "get",
  });
};

/**
 * 
 * @param currentPage 
 * @param pageSize 
 * @param data 
 * @returns 
 */
export const getNoteByDTO = (currentPage: number, pageSize: number, data: NoteDTO) => {
  return request<any>({
    url: `/web/es/note/getNoteByDTO/${currentPage}/${pageSize}`, // mock接口
    method: "post",
    data: data
  });
};

export const getCategoryAgg = (data: NoteDTO) => {
  return request<any>({
    url: `/web/es/note/getCategoryAgg`, // mock接口
    method: "post",
    data: data
  });
};

/**
 * 
 * @param keyword 
 * @returns 
 */
export const getRecordByKeyWord = (keyword: string, uid?: string) => {
  return request<any>({
    url: `/web/es/record/getRecordByKeyWord`,
    method: "get",
    params: {
      keyword,
      uid
    }
  });
};

/**
 * 
 * @returns 
 */
export const getHotRecord = () => {
  return request<any>({
    url: `/web/es/record/getHotRecord`, // mock接口
    method: "get",
  });
};

/**
 * 
 * @param keyword 
 * @returns 
 */
export const addRecord = (keyword: string, uid?: string) => {
  return request<any>({
    url: `/web/es/record/addRecord`,
    method: "post",
    data: {
      keyword,
      uid
    }
  });
};

export const deleteRecord = (keyword: string, uid?: string) => {
  return request<any>({
    url: `/web/es/record/clearRecordByUser`,
    method: "post",
    data: {
      keyword,
      uid
    }
  });
};

export const clearAllRecord = (uid?: string) => {
  return request<any>({
    url: `/web/es/record/clearRecordByUser`,
    method: "post",
    data: {
      uid
    }
  });
};

/**
 * 获取个性化推荐搜索词（猜你想搜）
 */
export const getGuessYouWant = (uid?: string) => {
  return request<any>({
    url: `/web/es/record/getGuessYouWant`,
    method: "get",
    params: {
      uid
    }
  });
};