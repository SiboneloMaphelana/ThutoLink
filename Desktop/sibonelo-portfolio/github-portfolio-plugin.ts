import type { Plugin } from 'vite'

const VIRTUAL_ID = 'virtual:portfolio-github'
const RESOLVED_ID = '\0' + VIRTUAL_ID

interface GithubPortfolioData {
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

interface GithubRepo {
  id: number
  name: string
  html_url: string
  description: string | null
  fork: boolean
  archived: boolean
  language: string | null
  topics?: string[]
  owner?: {
    login: string
  }
}

const MOBILE_TOPIC_HINTS = new Set([
  'mobile',
  'android',
  'ios',
  'react-native',
  'flutter',
  'kotlin',
  'swift',
])

function prettifyRepoName(name: string): string {
  return name
    .split(/[-_]/)
    .filter(Boolean)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ')
}

function inferProjectType(repo: GithubRepo): string {
  const topics = (repo.topics ?? []).map((t) => t.toLowerCase())
  if (topics.some((t) => MOBILE_TOPIC_HINTS.has(t))) return 'Mobile'
  return 'Web'
}

function buildStack(repo: GithubRepo): string[] {
  const topics = repo.topics ?? []
  const raw = [repo.language, ...topics].filter((x): x is string => Boolean(x))
  const seen = new Set<string>()
  const out: string[] = []
  for (const item of raw) {
    const key = item.toLowerCase()
    if (seen.has(key)) continue
    seen.add(key)
    out.push(item)
    if (out.length >= 8) break
  }
  return out
}

function transformRepos(repos: GithubRepo[]): Omit<GithubPortfolioData, 'fetchError'> {
  const own = repos.filter((r) => !r.fork && !r.archived)

  const langWeight = new Map<string, number>()
  for (const r of own) {
    if (r.language) {
      langWeight.set(r.language, (langWeight.get(r.language) ?? 0) + 1)
    }
  }
  if (langWeight.size === 0) {
    for (const r of repos) {
      if (r.language) {
        langWeight.set(r.language, (langWeight.get(r.language) ?? 0) + 1)
      }
    }
  }

  const skills = [...langWeight.entries()]
    .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
    .map(([lang]) => lang)

  const projects = own
    .slice()
    .sort((a, b) => a.name.localeCompare(b.name))
    .map((r) => ({
      id: r.id,
      name: r.name,
      type: inferProjectType(r),
      title: prettifyRepoName(r.name),
      description:
        r.description?.trim() ||
        `Public repository — ${prettifyRepoName(r.name)}.`,
      stack: buildStack(r),
      url: r.html_url,
    }))

  return { skills, projects }
}

function getGithubToken(): string | undefined {
  return (
    process.env.GITHUB_TOKEN ??
    process.env.GITHUB_FINE_GRAINED_TOKEN ??
    process.env.VITE_GITHUB_TOKEN ??
    undefined
  )
}

async function fetchJson<T>(url: string, token?: string): Promise<T> {
  const res = await fetch(url, {
    headers: {
      Accept: 'application/vnd.github+json',
      'X-GitHub-Api-Version': '2022-11-28',
      'User-Agent': 'sibonelo-portfolio-site',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(`GitHub ${res.status}: ${text.slice(0, 200)}`)
  }
  return res.json() as Promise<T>
}

async function fetchAllRepos(username: string, token?: string): Promise<GithubRepo[]> {
  const all: GithubRepo[] = []
  for (let page = 1; ; page++) {
    const url = token
      ? 'https://api.github.com/user/repos' +
        `?per_page=100&sort=updated&visibility=all` +
        `&affiliation=owner,collaborator,organization_member&page=${page}`
      : `https://api.github.com/users/${encodeURIComponent(username)}/repos` +
        `?per_page=100&sort=updated&type=owner&page=${page}`
    const batch = await fetchJson<GithubRepo[]>(url, token)
    all.push(...batch)
    if (batch.length < 100) break
  }
  return all
}

export function githubPortfolioPlugin(): Plugin {
  let cache: string | null = null

  return {
    name: 'github-portfolio',
    resolveId(id) {
      if (id === VIRTUAL_ID) return RESOLVED_ID
    },
    async load(id) {
      if (id !== RESOLVED_ID) return
      if (cache) return cache

      const username =
        process.env.VITE_GITHUB_USERNAME ?? 'SiboneloMaphelana'
      const token = getGithubToken()

      try {
        const repos = await fetchAllRepos(username, token)
        const data: GithubPortfolioData = transformRepos(repos)
        cache = `export default ${JSON.stringify(data)}`
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unknown error'
        console.warn('[github-portfolio]', message)
        const fallback: GithubPortfolioData = {
          skills: [],
          projects: [],
          fetchError: message,
        }
        cache = `export default ${JSON.stringify(fallback)}`
      }
      return cache
    },
  }
}
