import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type FormEvent,
  type MouseEvent,
} from 'react'
import { Icon } from '@iconify/react'
import './App.css'
import githubPortfolio from 'virtual:portfolio-github'
import sbudaImage from './assets/sbuda.png'
import sbudaAboutImage from './assets/sbuda2.png'

const navItems = [
  { label: 'Home', href: '#home' },
  { label: 'About', href: '#about' },
  { label: 'Skills', href: '#skills' },
  { label: 'Experience', href: '#experience' },
  { label: 'Projects', href: '#projects' },
  { label: 'Contact', href: '#contact' },
] as const

const sectionIds = navItems.map(
  (item) => item.href.replace('#', '')
) as readonly string[]

const socialLinks = [
  {
    label: 'GitHub',
    href: 'https://github.com/SiboneloMaphelana',
    icon: 'mdi:github',
    external: true,
  },
  {
    label: 'Contact',
    href: '#contact',
    icon: 'mdi:email-fast-outline',
    external: false,
  },
] as const

const experience = [
  {
    company: 'Independent Product Work',
    role: 'Fullstack Developer',
    period: '2023 - Present',
    location: 'Remote',
    bullets: [
      'Built complete web products from landing pages to API integrations, auth flows, and internal dashboards.',
      'Shipped responsive interfaces with careful typography, strong visual hierarchy, and clean component systems.',
      'Delivered backend features for data storage, payments, content management, and deployment workflows.',
    ],
  },
]

const heroKickers = [
  "Hi, I'm Sibonelo",
  'Fullstack developer building polished digital products',
  'Available for freelance work and thoughtful collaborations',
  'Strong UX, clean systems, and fast iteration loops',
]

const focusCards = [
  {
    id: 'building',
    label: 'Product',
    title: 'Design-aware digital products',
    detail:
      'I build fullstack products with strong frontend craft, dependable backend logic, and a clear sense of what users need to get done.',
  },
  {
    id: 'available',
    label: 'Delivery',
    title: 'Freelance, contract, and team support',
    detail:
      'Whether you need a focused build sprint or extra engineering capacity, I work in a way that keeps momentum high and handoff clean.',
  },
  {
    id: 'base',
    label: 'Workflow',
    title: 'Remote-first and collaboration-friendly',
    detail:
      'Based in South Africa and comfortable across time zones, with async-friendly communication, clean code, and reliable follow-through.',
  },
] as const

const playfulBadges = [
  'Responsive UI systems',
  'Reliable API integrations',
  'Thoughtful product polish',
] as const

const quickFacts = [
  { label: 'Build Mode', value: 'Structured + creative' },
  { label: 'Favorite Loop', value: 'Research, build, refine' },
  { label: 'Best With', value: 'Ambitious teams and product ideas' },
] as const

type ProjectFilter = 'All' | 'Mobile' | 'Web'
type FocusCardId = (typeof focusCards)[number]['id']
type ContactFormState = {
  name: string
  email: string
  message: string
}

function prefersReducedMotion(): boolean {
  if (typeof window === 'undefined') return false
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function App() {
  const { skills, projects, fetchError } = githubPortfolio
  const reducedMotion = useMemo(() => prefersReducedMotion(), [])
  const [projectFilter, setProjectFilter] = useState<ProjectFilter>('All')
  const [activeSection, setActiveSection] = useState(sectionIds[0])
  const [kickerIndex, setKickerIndex] = useState(0)
  const [contactFlash, setContactFlash] = useState(false)
  const [skillsVisible, setSkillsVisible] = useState(reducedMotion)
  const [projectsVisible, setProjectsVisible] = useState(reducedMotion)
  const [activeFocusCard, setActiveFocusCard] = useState<FocusCardId>(focusCards[0].id)
  const [activeSkill, setActiveSkill] = useState<string | null>(null)
  const [contactForm, setContactForm] = useState<ContactFormState>({
    name: '',
    email: '',
    message: '',
  })
  const [toastMessage, setToastMessage] = useState(
    "Message saved for demo — connect this form to your email or API when you're ready."
  )

  const avatarRef = useRef<HTMLDivElement>(null)
  const heroRef = useRef<HTMLElement>(null)
  const skillsSectionRef = useRef<HTMLElement>(null)
  const projectsSectionRef = useRef<HTMLElement>(null)

  const filteredProjects = useMemo(() => {
    return projects.filter((project) => {
      const matchesType = projectFilter === 'All' || project.type === projectFilter
      const matchesSkill =
        activeSkill === null ||
        project.stack.some((item) => item.toLowerCase() === activeSkill.toLowerCase())
      return matchesType && matchesSkill
    })
  }, [activeSkill, projectFilter, projects])

  const activeFocusContent = useMemo(
    () => focusCards.find((card) => card.id === activeFocusCard) ?? focusCards[0],
    [activeFocusCard]
  )

  const heroProofs = useMemo(
    () => [
      {
        icon: 'mdi:briefcase-variant-outline',
        label: 'What I do',
        value: 'Fullstack product development',
      },
      {
        icon: 'mdi:monitor-cellphone-star',
        label: 'Primary focus',
        value: 'Responsive web experiences',
      },
      {
        icon: 'mdi:source-repository',
        label: 'Selected work',
        value:
          projects.length > 0
            ? `${projects.length} GitHub projects surfaced`
            : 'Projects curated from GitHub data',
      },
    ],
    [projects.length]
  )

  const formStatus = useMemo(() => {
    const nameValid = contactForm.name.trim().length >= 2
    const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contactForm.email.trim())
    const messageLength = contactForm.message.trim().length
    const messageValid = messageLength >= 20

    return {
      nameValid,
      emailValid,
      messageValid,
      messageLength,
      isValid: nameValid && emailValid && messageValid,
    }
  }, [contactForm])

  useEffect(() => {
    const updateActive = () => {
      const marker = window.scrollY + window.innerHeight * 0.28
      let next = sectionIds[0]
      for (const id of sectionIds) {
        const el = document.getElementById(id)
        if (el && el.offsetTop <= marker) next = id
      }
      setActiveSection(next)
    }

    window.addEventListener('scroll', updateActive, { passive: true })
    window.addEventListener('resize', updateActive, { passive: true })
    updateActive()
    return () => {
      window.removeEventListener('scroll', updateActive)
      window.removeEventListener('resize', updateActive)
    }
  }, [])

  useEffect(() => {
    if (reducedMotion) return

    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return
          if (entry.target === skillsSectionRef.current) setSkillsVisible(true)
          if (entry.target === projectsSectionRef.current)
            setProjectsVisible(true)
        })
      },
      { rootMargin: '0px 0px -12% 0px', threshold: 0.08 }
    )

    const s = skillsSectionRef.current
    const p = projectsSectionRef.current
    if (s) io.observe(s)
    if (p) io.observe(p)
    return () => io.disconnect()
  }, [reducedMotion])

  const onHeroPointerMove = useCallback(
    (e: MouseEvent<HTMLElement>) => {
      if (reducedMotion || !avatarRef.current || !heroRef.current) return
      const rect = heroRef.current.getBoundingClientRect()
      const pointerX = e.clientX - rect.left
      const pointerY = e.clientY - rect.top
      const cx = rect.left + rect.width / 2
      const cy = rect.top + rect.height / 2
      const dx = (e.clientX - cx) / rect.width
      const dy = (e.clientY - cy) / rect.height
      const max = 10
      heroRef.current.style.setProperty('--pointer-x', `${pointerX}px`)
      heroRef.current.style.setProperty('--pointer-y', `${pointerY}px`)
      avatarRef.current.style.setProperty('--parallax-x', `${dx * max}px`)
      avatarRef.current.style.setProperty('--parallax-y', `${dy * max}px`)
    },
    [reducedMotion]
  )

  const onHeroPointerLeave = useCallback(() => {
    if (!avatarRef.current || !heroRef.current) return
    heroRef.current.style.setProperty('--pointer-x', '50%')
    heroRef.current.style.setProperty('--pointer-y', '38%')
    avatarRef.current.style.setProperty('--parallax-x', '0px')
    avatarRef.current.style.setProperty('--parallax-y', '0px')
  }, [])

  const onProjectMove = useCallback(
    (e: MouseEvent<HTMLElement>) => {
      if (reducedMotion) return
      const card = e.currentTarget
      const r = card.getBoundingClientRect()
      const px = (e.clientX - r.left) / r.width
      const py = (e.clientY - r.top) / r.height
      const rx = (py - 0.5) * -10
      const ry = (px - 0.5) * 10
      card.style.setProperty('--pointer-x', `${px * 100}%`)
      card.style.setProperty('--pointer-y', `${py * 100}%`)
      card.style.setProperty('--tilt-x', `${rx}deg`)
      card.style.setProperty('--tilt-y', `${ry}deg`)
    },
    [reducedMotion]
  )

  const onProjectLeave = useCallback((e: MouseEvent<HTMLElement>) => {
    const card = e.currentTarget
    card.style.setProperty('--pointer-x', '50%')
    card.style.setProperty('--pointer-y', '50%')
    card.style.setProperty('--tilt-x', '0deg')
    card.style.setProperty('--tilt-y', '0deg')
  }, [])

  const cycleKicker = useCallback(() => {
    setKickerIndex((i) => (i + 1) % heroKickers.length)
  }, [])

  const toggleSkill = useCallback((skill: string) => {
    setActiveSkill((current) => (current === skill ? null : skill))
  }, [])

  const onContactChange = useCallback(
    (field: keyof ContactFormState, value: string) => {
      setContactForm((current) => ({ ...current, [field]: value }))
    },
    []
  )

  const onContactSubmit = useCallback((e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!formStatus.isValid) return
    setContactFlash(true)
    setToastMessage(
      `Thanks, ${contactForm.name.trim()}. Your message has been saved locally for this demo.`
    )
    window.setTimeout(() => setContactFlash(false), 3200)
    setContactForm({ name: '', email: '', message: '' })
  }, [contactForm.name, formStatus.isValid])

  return (
    <div className="page-shell">
      <header className="site-header">
        <a className="brand-mark" href="#home" aria-label="Sibonelo home">
          <img src={sbudaImage} alt="Sibonelo portrait" className="brand-image" />
        </a>

        <nav className="nav-pill" aria-label="Primary">
          {navItems.map((item) => {
            const id = item.href.replace('#', '')
            return (
              <a
                key={item.label}
                href={item.href}
                className={activeSection === id ? 'nav-active' : undefined}
                aria-current={activeSection === id ? 'location' : undefined}
              >
                {item.label}
              </a>
            )
          })}
        </nav>
      </header>

      <main>
        {fetchError ? (
          <p className="github-data-notice github-data-banner" role="alert">
            Could not load GitHub portfolio data: {fetchError}. Check{' '}
            <code>VITE_GITHUB_USERNAME</code>, <code>GITHUB_TOKEN</code>, and your
            network, then rebuild.
          </p>
        ) : null}

        <section
          ref={heroRef}
          className="hero-section"
          id="home"
          onMouseMove={onHeroPointerMove}
          onMouseLeave={onHeroPointerLeave}
        >
          <div className="hero-spark hero-spark-one" aria-hidden="true" />
          <div className="hero-spark hero-spark-two" aria-hidden="true" />
          <div className="hero-spark hero-spark-three" aria-hidden="true" />

          <div className="avatar-wrap">
            <div ref={avatarRef} className="avatar-orb avatar-orb-interactive">
              <img src={sbudaImage} alt="Sibonelo portrait" className="avatar-image" />
            </div>
          </div>

          <button
            type="button"
            className="hero-kicker hero-kicker-btn"
            onClick={cycleKicker}
            aria-label="Cycle welcome message"
          >
            {heroKickers[kickerIndex]}
          </button>
          <div className="hero-mini-note">Professional execution with a human touch.</div>
          <div className="hero-lead-in">Fullstack developer based in South Africa</div>
          <p className="hero-copy">
            I design and build fullstack experiences that balance personality with
            professionalism, helping products feel polished, intuitive, and ready for real use.
          </p>

          <div className="hero-proof-grid" aria-label="Professional highlights">
            {heroProofs.map((item) => (
              <article key={item.label} className="hero-proof-card">
                <Icon icon={item.icon} aria-hidden="true" />
                <span>{item.label}</span>
                <strong>{item.value}</strong>
              </article>
            ))}
          </div>

          <div className="hero-focus-panel" aria-label="Portfolio highlights">
            <div className="hero-focus-tabs" role="tablist" aria-label="Focus areas">
              {focusCards.map((card) => (
                <button
                  key={card.id}
                  type="button"
                  role="tab"
                  aria-selected={activeFocusCard === card.id}
                  className={`hero-focus-tab${activeFocusCard === card.id ? ' hero-focus-tab-active' : ''}`}
                  onClick={() => setActiveFocusCard(card.id)}
                >
                  {card.label}
                </button>
              ))}
            </div>
            <div className="hero-focus-content">
              <strong>{activeFocusContent.title}</strong>
              <p>{activeFocusContent.detail}</p>
            </div>
          </div>

          <div className="hero-badge-row" aria-label="Highlights">
            {playfulBadges.map((badge) => (
              <span key={badge} className="hero-badge">
                {badge}
              </span>
            ))}
          </div>

          <div className="social-row" aria-label="Social links">
            {socialLinks.map((item) => (
              <a
                key={item.label}
                href={item.href}
                aria-label={item.label}
                target={item.external ? '_blank' : undefined}
                rel={item.external ? 'noopener noreferrer' : undefined}
              >
                <Icon icon={item.icon} aria-hidden="true" />
              </a>
            ))}
          </div>

          <div className="hero-actions">
            <a href="#contact" className="outline-button pressable">
              Start a Project
            </a>
            <a href="#projects" className="filled-button pressable">
              View Selected Work
            </a>
          </div>
        </section>

        <section className="content-section about-section" id="about">
          <div className="section-heading">
            <p>Introduction</p>
            <h2 className="glitch glitch-hover" data-text="About me">
              About me
            </h2>
          </div>

          <p className="section-helper">
            I care about interfaces that feel warm and distinctive, while staying clear, useful, and easy to trust.
          </p>

          <div className="about-grid">
            <div className="portrait-card portrait-card-lift">
              <img
                src={sbudaAboutImage}
                alt="Sibonelo portrait"
                className="portrait-image"
              />
            </div>

            <div className="about-copy">
              <p>
                I&apos;m a fullstack developer focused on building modern digital
                products that look refined, perform well, and support real business needs from the first interaction to deployment.
              </p>
              <p>
                My work spans frontend and backend development, from responsive interfaces and motion details to APIs, integrations, and practical systems behind the scenes.
              </p>
              <p>
                I enjoy fast-moving environments, whether I&apos;m working independently or inside a team, and I bring a calm, product-minded approach to shipping quality work.
              </p>
            </div>
          </div>

          <div className="quick-facts">
            {quickFacts.map((fact) => (
              <article key={fact.label} className="fact-card">
                <span>{fact.label}</span>
                <strong>{fact.value}</strong>
              </article>
            ))}
          </div>
        </section>

        <section
          ref={skillsSectionRef}
          className={`content-section skills-section${skillsVisible ? ' section-revealed' : ''}`}
          id="skills"
        >
          <div className="section-heading">
            <p>Skills</p>
            <h2 className="glitch glitch-hover" data-text="What I Know">
              What I Know
            </h2>
          </div>

          <p className="section-helper">
            Select a skill to filter the project list by the tools behind the work.
          </p>

          {skills.length === 0 && !fetchError ? (
            <p className="github-data-notice" role="status">
              No primary languages found on your public repositories yet.
            </p>
          ) : null}

          <div className="skills-grid">
            {skills.map((skill) => (
              <button
                key={skill}
                type="button"
                className={`skill-icon skill-icon-pop skill-button${activeSkill === skill ? ' skill-active' : ''}`}
                onClick={() => toggleSkill(skill)}
                aria-pressed={activeSkill === skill}
              >
                <span>{skill.slice(0, 2).toUpperCase()}</span>
                <strong>{skill}</strong>
              </button>
            ))}
          </div>
        </section>

        <section className="content-section experience-section" id="experience">
          <div className="section-heading">
            <p>Experience</p>
            <h2 className="glitch glitch-hover" data-text="My Journey">
              My Journey
            </h2>
          </div>

          <div className="timeline-layout">
            <div className="timeline-rail">
              <span className="timeline-dot timeline-dot-pulse" />
            </div>

            {experience.map((item) => (
              <article key={item.company} className="experience-card">
                <div className="experience-company">{item.company}</div>
                <div className="experience-details">
                  <div className="experience-header">
                    <div>
                      <h3>{item.role}</h3>
                      <p className="experience-meta">
                        {item.period} <span>&middot;</span> {item.location}
                      </p>
                    </div>
                  </div>

                  <ul>
                    {item.bullets.map((bullet) => (
                      <li key={bullet}>{bullet}</li>
                    ))}
                  </ul>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section
          ref={projectsSectionRef}
          className={`content-section projects-section${projectsVisible ? ' section-revealed' : ''}`}
          id="projects"
        >
          <div className="section-heading">
            <p>Projects</p>
            <h2 className="glitch glitch-hover" data-text="What I&apos;ve built">
              What I&apos;ve built
            </h2>
          </div>

          <div className="filter-bar" aria-label="Project filters">
            {(['All', 'Mobile', 'Web'] as const).map((label) => (
              <button
                key={label}
                type="button"
                className={`filter-chip${projectFilter === label ? ' filter-active' : ''}`}
                onClick={() => setProjectFilter(label)}
              >
                {label}
              </button>
            ))}
          </div>

          <div className="project-toolbar">
            <p className="section-helper">
              {activeSkill
                ? `Showing ${projectFilter.toLowerCase()} projects tagged with ${activeSkill}.`
                : `Showing ${projectFilter.toLowerCase()} projects across selected work from the portfolio.`}
            </p>
            {activeSkill ? (
              <button
                type="button"
                className="clear-filter-button"
                onClick={() => setActiveSkill(null)}
              >
                Clear skill filter
              </button>
            ) : null}
          </div>

          {!fetchError && projects.length === 0 ? (
            <p className="github-data-notice" role="status">
              No public owner repositories to show yet.
            </p>
          ) : null}

          {!fetchError && projects.length > 0 && filteredProjects.length === 0 ? (
            <p className="github-data-notice" role="status">
              No projects match the current type and skill filters yet.
            </p>
          ) : null}

          <div className="project-grid">
            {filteredProjects.map((project) => (
              <article
                key={project.id}
                className="project-card project-card-tilt"
                onMouseMove={onProjectMove}
                onMouseLeave={onProjectLeave}
              >
                <div className="project-preview">
                  <div className="project-badge">{project.type}</div>
                  <div className="preview-window">
                    <div className="preview-nav">
                      <span />
                      <span />
                      <span />
                    </div>
                    <div className="preview-body">
                      <div className="preview-bar short" />
                      <div className="preview-bar medium" />
                      <div className="preview-grid">
                        <span />
                        <span />
                        <span />
                      </div>
                    </div>
                  </div>
                </div>

                <div className="project-content">
                  <div className="project-heading">
                    <h3>{project.title}</h3>
                    <a
                      href={project.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      aria-label={`View ${project.title} on GitHub`}
                    >
                      ↗
                    </a>
                  </div>
                  <p>{project.description}</p>
                  <div className="tag-row">
                    {project.stack.map((item) => (
                      <span key={item}>{item}</span>
                    ))}
                  </div>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="content-section contact-section" id="contact">
          <div className="section-heading">
            <p>Contact</p>
            <h2 className="glitch glitch-hover" data-text="Say hello">
              Say hello
            </h2>
          </div>

          <p className="section-helper">
            If you need a developer who cares about both implementation quality and user experience, let&apos;s talk.
          </p>

          <form className="contact-form" onSubmit={onContactSubmit}>
            <input
              type="text"
              name="name"
              placeholder="Enter your name"
              value={contactForm.name}
              onChange={(e) => onContactChange('name', e.target.value)}
            />
            <input
              type="email"
              name="email"
              placeholder="Enter your email address"
              value={contactForm.email}
              onChange={(e) => onContactChange('email', e.target.value)}
            />
            <label className="message-field">
              <textarea
                name="message"
                placeholder="Enter your message"
                rows={8}
                value={contactForm.message}
                onChange={(e) => onContactChange('message', e.target.value)}
              />
              <span
                className={`message-counter${formStatus.messageValid ? ' message-counter-valid' : ''}`}
              >
                {formStatus.messageLength}/20 characters minimum
              </span>
            </label>
            <div className="form-hints" aria-live="polite">
              <span className={formStatus.nameValid ? 'hint-valid' : ''}>
                Name: {formStatus.nameValid ? 'looks good' : 'at least 2 characters'}
              </span>
              <span className={formStatus.emailValid ? 'hint-valid' : ''}>
                Email: {formStatus.emailValid ? 'valid format' : 'enter a valid email'}
              </span>
              <span className={formStatus.messageValid ? 'hint-valid' : ''}>
                Message: {formStatus.messageValid ? 'ready to send' : 'write at least 20 characters'}
              </span>
            </div>
            <button
              type="submit"
              className="outline-button submit-button pressable"
              disabled={!formStatus.isValid}
            >
              Send Message
            </button>
            <p
              className={`contact-toast${contactFlash ? ' contact-toast-visible' : ''}`}
              role="status"
              aria-live="polite"
            >
              <span className="contact-toast-emoji" aria-hidden>
                ✨
              </span>{' '}
              {toastMessage}
            </p>
          </form>
        </section>
      </main>
    </div>
  )
}

export default App
