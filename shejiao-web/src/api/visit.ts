import request from "@/utils/request";

export const clickNote = (userUid: string, noteUid: string) => {
  return request<any>({
    url: "/web/visit/clickNote",
    method: "post",
    params: {
      userUid,
      noteUid,
    },
  });
};

export const search = (userUid: string, keyword: string) => {
  return request<any>({
    url: "/web/visit/search",
    method: "post",
    params: {
      userUid,
      keyword,
    },
  });
};

export const clickTag = (userUid: string, tagUid: string) => {
  return request<any>({
    url: "/web/visit/clickTag",
    method: "post",
    params: {
      userUid,
      tagUid,
    },
  });
};

export const clickCategory = (userUid: string, categoryUid: string) => {
  return request<any>({
    url: "/web/visit/clickCategory",
    method: "post",
    params: {
      userUid,
      categoryUid,
    },
  });
};
