/// <reference types="vite/client" />

declare module 'virtual:portfolio-github' {
  const data: {
    skills: string[]
    projects: Array<{
      id: number
      name: string
      type: string
      title: string
      description: string
      stack: string[]
      url: string
    }>
    fetchError?: string
  }
  export default data
}
