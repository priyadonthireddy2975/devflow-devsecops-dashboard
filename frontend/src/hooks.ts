import { useCallback, useEffect, useRef, useState } from 'react'

export function useAsync<T>(loader: () => Promise<T>, deps: unknown[], pollMs?: number) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const loaderRef = useRef(loader)
  loaderRef.current = loader

  const run = useCallback(async (silent: boolean) => {
    if (!silent) setLoading(true)
    try {
      const result = await loaderRef.current()
      setData(result)
      setError(null)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void run(false)
    if (!pollMs) return
    const id = window.setInterval(() => void run(true), pollMs)
    return () => window.clearInterval(id)
  }, [run, pollMs, ...deps])

  return { data, error, loading, reload: () => run(false) }
}
