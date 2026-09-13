import { useState, useEffect } from 'react';
import { Newspaper, Calendar, ShieldCheck, Clock } from 'lucide-react';
import { api } from '@/lib/api';
import type { CorporateEventDTO, NewsArticleDTO } from '@/types/terminal';

interface NewsTimelinePanelProps {
  symbol: string;
}

export function NewsTimelinePanel({ symbol }: NewsTimelinePanelProps) {
  const [events, setEvents] = useState<CorporateEventDTO[]>([]);
  const [articles, setArticles] = useState<NewsArticleDTO[]>([]);
  const [activeTab, setActiveTab] = useState<'all' | 'events' | 'filings'>('all');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      api.get<CorporateEventDTO[]>(`/v1/news/events/${symbol}`).catch(() => []),
      api.get<NewsArticleDTO[]>(`/v1/news/articles/${symbol}`).catch(() => []),
    ]).then(([evts, arts]) => {
      setEvents(evts || []);
      setArticles(arts || []);
    }).finally(() => setLoading(false));
  }, [symbol]);

  const hasData = events.length > 0 || articles.length > 0;

  return (
    <div className="rounded-xl border border-border bg-surface p-4 flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-border/50 pb-2.5 mb-3">
        <div className="flex items-center gap-2">
          <Newspaper className="h-4 w-4 text-accent" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-text-primary">
            {symbol} Corporate Intelligence & Regulatory Filings
          </h3>
        </div>

        <div className="flex items-center gap-2">
          <span className="inline-flex items-center gap-1 text-[10px] font-mono px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
            <ShieldCheck className="h-3 w-3" />
            PIT Enforced
          </span>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 border-b border-border pb-2 mb-3 text-xs">
        <button
          onClick={() => setActiveTab('all')}
          className={`px-3 py-1 rounded font-medium transition-colors ${
            activeTab === 'all'
              ? 'bg-accent-muted text-accent'
              : 'text-text-muted hover:text-text-primary'
          }`}
        >
          All Timeline ({events.length + articles.length})
        </button>
        <button
          onClick={() => setActiveTab('events')}
          className={`px-3 py-1 rounded font-medium transition-colors ${
            activeTab === 'events'
              ? 'bg-accent-muted text-accent'
              : 'text-text-muted hover:text-text-primary'
          }`}
        >
          Corporate Disclosures ({events.length})
        </button>
        <button
          onClick={() => setActiveTab('filings')}
          className={`px-3 py-1 rounded font-medium transition-colors ${
            activeTab === 'filings'
              ? 'bg-accent-muted text-accent'
              : 'text-text-muted hover:text-text-primary'
          }`}
        >
          News & Media ({articles.length})
        </button>
      </div>

      {/* Timeline Stream */}
      <div className="flex-1 overflow-y-auto space-y-3 pr-1 max-h-[360px]">
        {loading && (
          <div className="py-8 text-center text-xs text-text-muted animate-pulse">
            Loading corporate events & filings for {symbol}...
          </div>
        )}

        {!loading && !hasData && (
          <div className="py-8 text-center text-xs text-text-muted">
            <Calendar className="h-6 w-6 mx-auto mb-2 opacity-40" />
            No recent corporate filings or disclosures recorded for {symbol}.
          </div>
        )}

        {!loading &&
          events.map((evt) => (
            <div
              key={`evt-${evt.id}`}
              className="p-3 rounded-lg border border-border/60 bg-surface-elevated/40 hover:bg-surface-elevated transition-colors"
            >
              <div className="flex items-center justify-between gap-2 mb-1">
                <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-purple-500/10 text-purple-400 border border-purple-500/20 font-semibold">
                  {evt.eventType}
                </span>
                <span className="text-[10px] text-text-muted flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  Event: {new Date(evt.eventDate).toLocaleDateString('en-IN')}
                </span>
              </div>

              <h4 className="text-xs font-bold text-text-primary mt-1">{evt.headline}</h4>
              {evt.details && <p className="text-xs text-text-secondary mt-1 line-clamp-2">{evt.details}</p>}

              <div className="mt-2.5 flex items-center justify-between text-[10px] text-text-muted border-t border-border/30 pt-1.5">
                <span>Source: <strong className="text-text-secondary">{evt.source}</strong></span>
                <span>Available at: <strong className="text-text-secondary">{new Date(evt.informationAvailableAt).toLocaleString('en-IN')}</strong></span>
              </div>
            </div>
          ))}

        {!loading &&
          articles.map((art) => (
            <div
              key={`art-${art.id}`}
              className="p-3 rounded-lg border border-border/60 bg-surface-elevated/40 hover:bg-surface-elevated transition-colors"
            >
              <div className="flex items-center justify-between gap-2 mb-1">
                <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 font-semibold">
                  NEWS
                </span>
                <span className="text-[10px] text-text-muted flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  Published: {new Date(art.publishedAt).toLocaleString('en-IN')}
                </span>
              </div>

              <h4 className="text-xs font-bold text-text-primary mt-1">{art.headline}</h4>
              {art.summary && <p className="text-xs text-text-secondary mt-1 line-clamp-2">{art.summary}</p>}

              <div className="mt-2.5 flex items-center justify-between text-[10px] text-text-muted border-t border-border/30 pt-1.5">
                <span>Source: <strong className="text-text-secondary">{art.source}</strong></span>
                <span>PIT Available: <strong className="text-text-secondary">{new Date(art.informationAvailableAt).toLocaleTimeString('en-IN')}</strong></span>
              </div>
            </div>
          ))}
      </div>
    </div>
  );
}
