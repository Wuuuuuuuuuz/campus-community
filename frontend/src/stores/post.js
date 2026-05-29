import { defineStore } from 'pinia'
import request from '../utils/request'

export const usePostStore = defineStore('post', () => {

  async function fetchPosts(params) {
    return await request.get('/posts', { params })
  }

  async function fetchPost(id) {
    return await request.get(`/posts/${id}`)
  }

  async function createPost(data) {
    return await request.post('/posts', data)
  }

  async function updatePost(id, data) {
    return await request.put(`/posts/${id}`, data)
  }

  async function deletePost(id) {
    return await request.delete(`/posts/${id}`)
  }

  async function fetchComments(postId) {
    return await request.get(`/posts/${postId}/comments`)
  }

  async function createComment(postId, data) {
    return await request.post(`/posts/${postId}/comments`, data)
  }

  async function deleteComment(id) {
    return await request.delete(`/comments/${id}`)
  }

  async function fetchCategories() {
    return await request.get('/categories')
  }

  async function createCategory(data) {
    return await request.post('/categories', data)
  }

  async function updateCategory(id, data) {
    return await request.put(`/categories/${id}`, data)
  }

  async function deleteCategory(id) {
    return await request.delete(`/categories/${id}`)
  }

  async function updateProfile(data) {
    return await request.put('/users/profile', data)
  }

  return {
    fetchPosts, fetchPost, createPost, updatePost, deletePost,
    fetchComments, createComment, deleteComment,
    fetchCategories, createCategory, updateCategory, deleteCategory,
    updateProfile
  }
})
