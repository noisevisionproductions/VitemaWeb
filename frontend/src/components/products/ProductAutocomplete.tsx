import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useDebounce } from '../../hooks/useDebounce';
import { ProductService } from '../../services/product/ProductService';
import type { ProductDb } from '../../types/product';
import { Loader2, Package, Plus, Search } from 'lucide-react';

export interface ProductAutocompleteProps {
  onSelect: (product: ProductDb) => void;
  onFreeText?: (name: string) => void;
  placeholder?: string;
  className?: string;
}

function formatMacros(product: ProductDb): string {
  const per = product.unit ? `100${product.unit}` : '100g';
  return `${Math.round(product.kcal)}kcal/${per}`;
}

const ProductAutocomplete: React.FC<ProductAutocompleteProps> = ({
  onSelect,
  onFreeText,
  placeholder = 'Szukaj produktu (np. kurczak, ryż)...',
  className = '',
}) => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<ProductDb[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const debouncedQuery = useDebounce(query, 300);

  const runSearch = useCallback(async (q: string) => {
    if (!q.trim()) {
      setResults([]);
      return;
    }
    setLoading(true);
    try {
      const list = await ProductService.search(q);
      setResults(list);
      setOpen(true);
      setSelectedIndex(-1);
    } catch (e) {
      console.error('Product search failed:', e);
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    runSearch(debouncedQuery);
  }, [debouncedQuery, runSearch]);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const hasFreeText = Boolean(onFreeText && query.trim());
  const totalOptions = results.length + (hasFreeText ? 1 : 0);

  const handleSelect = useCallback(
    (product: ProductDb) => {
      onSelect(product);
      setQuery('');
      setResults([]);
      setOpen(false);
      setSelectedIndex(-1);
      inputRef.current?.focus();
    },
    [onSelect]
  );

  const handleFreeText = useCallback(() => {
    const name = query.trim();
    if (!name || !onFreeText) return;
    onFreeText(name);
    setQuery('');
    setResults([]);
    setOpen(false);
    setSelectedIndex(-1);
    inputRef.current?.focus();
  }, [query, onFreeText]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!open && totalOptions === 0) return;
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setSelectedIndex((i) => (i < totalOptions - 1 ? i + 1 : i));
        break;
      case 'ArrowUp':
        e.preventDefault();
        setSelectedIndex((i) => (i > -1 ? i - 1 : -1));
        break;
      case 'Enter':
        e.preventDefault();
        if (selectedIndex >= 0 && selectedIndex < results.length) {
          handleSelect(results[selectedIndex]);
        } else if (hasFreeText && selectedIndex === results.length) {
          handleFreeText();
        }
        break;
      case 'Escape':
        setOpen(false);
        setSelectedIndex(-1);
        break;
      default:
        break;
    }
  };

  return (
    <div ref={containerRef} className={`relative ${className}`}>
      <div className="relative">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
        <input
          ref={inputRef}
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => (results.length > 0 || hasFreeText) && setOpen(true)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          className="w-full rounded-lg border border-gray-300 bg-white py-2 pl-10 pr-10 text-sm text-gray-900 shadow-sm transition-shadow focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
        />
        {loading && (
          <Loader2 className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 animate-spin text-gray-400" />
        )}
        {!loading && hasFreeText && (
          <button
            type="button"
            onClick={handleFreeText}
            className="absolute right-3 top-1/2 -translate-y-1/2 rounded p-1 text-gray-500 hover:bg-gray-100 hover:text-gray-700"
            title="Dodaj jako dowolny składnik"
          >
            <Plus className="h-4 w-4" />
          </button>
        )}
      </div>

      {open && (results.length > 0 || hasFreeText) && (
        <div className="absolute left-0 right-0 top-full z-50 mt-1 max-h-64 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-lg">
          <div className="flex items-center gap-2 border-b border-gray-100 bg-gray-50 px-3 py-2">
            <Package className="h-3.5 w-3.5 text-gray-500" />
            <span className="text-xs font-medium text-gray-500">
              {results.length > 0 ? 'Wybierz produkt' : 'Dodaj dowolny składnik'}
            </span>
          </div>
          <div className="max-h-48 overflow-y-auto">
            {hasFreeText && (
              <button
                type="button"
                onClick={handleFreeText}
                className={`flex w-full items-center gap-2 border-b border-gray-100 px-3 py-2.5 text-left transition-colors ${
                  selectedIndex === results.length ? 'bg-primary/10 text-primary' : 'hover:bg-gray-50'
                }`}
              >
                <div className="rounded bg-primary/10 p-1">
                  <Plus className="h-4 w-4 text-primary" />
                </div>
                <div>
                  <div className="text-sm font-medium">Dodaj &quot;{query.trim()}&quot;</div>
                  <div className="text-xs text-gray-500">Bez produktu z bazy</div>
                </div>
              </button>
            )}
            {results.map((product, index) => (
              <button
                key={String(product.id)}
                type="button"
                onClick={() => handleSelect(product)}
                className={`flex w-full flex-col items-start gap-0.5 border-b border-gray-100 px-3 py-2.5 text-left last:border-b-0 transition-colors ${
                  index === selectedIndex ? 'border-l-4 border-l-primary bg-primary/5 pl-2' : 'hover:bg-gray-50'
                }`}
              >
                <span className="text-sm font-medium text-gray-900">{product.name}</span>
                <span className="text-xs text-gray-500">{formatMacros(product)}</span>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductAutocomplete;
