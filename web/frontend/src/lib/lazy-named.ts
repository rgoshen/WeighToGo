import { lazy, type ComponentType, type LazyExoticComponent } from 'react';

/**
 * Wraps {@link lazy} for modules whose component is a *named* export.
 *
 * React's `lazy` requires the loaded module's `.default` to be the component
 * (per the React docs, the load function must "resolve to an object whose
 * `.default` property is a valid React component type"). The app's page
 * modules use named exports, so this helper maps the chosen named export onto
 * `default` — avoiding a churn of default-export conversions that would break
 * the existing named imports in tests and `main.tsx`.
 *
 * @param loader - Dynamic import of the module, e.g. `() => import('./Page')`.
 * @param exportName - Name of the component export within that module.
 * @returns A lazy component suitable for rendering inside `<Suspense>`.
 */
export function lazyNamed<TModule, TKey extends keyof TModule>(
  loader: () => Promise<TModule>,
  exportName: TKey,
): LazyExoticComponent<
  TModule[TKey] extends ComponentType<infer TProps> ? ComponentType<TProps> : never
> {
  return lazy(async () => {
    const loaded = await loader();
    return { default: loaded[exportName] as ComponentType<unknown> };
  }) as LazyExoticComponent<
    TModule[TKey] extends ComponentType<infer TProps> ? ComponentType<TProps> : never
  >;
}
