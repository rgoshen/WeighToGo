/**
 * Main application shell layout.
 *
 * Provides a persistent MUI AppBar at the top, a collapsible side Drawer
 * on desktop (hamburger toggle on mobile), and an <Outlet /> from React
 * Router that renders the active route's page component.
 *
 * SRS §10.2 specifies the AppLayout behaviour.
 */

import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  SvgIcon,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';

/**
 * Inline hamburger menu icon — avoids a hard dependency on
 * \@mui/icons-material during the Phase 5 scaffold.
 */
function MenuIcon() {
  return (
    <SvgIcon>
      <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z" />
    </SvgIcon>
  );
}
import { Suspense, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';

import { protectedRoutes } from '../routes';
import { ErrorBoundary } from './ErrorBoundary';
import { LoadingSplash } from './LoadingSplash';
import { NavList } from './NavList';
import { UserMenu } from './UserMenu';

const DRAWER_WIDTH = 240;

/**
 * Renders the persistent application shell with top bar, side nav, and
 * page content outlet.
 */
export function AppLayout() {
  const muiTheme = useTheme();
  const isDesktop = useMediaQuery(muiTheme.breakpoints.up('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  function handleDrawerToggle() {
    setMobileOpen((prev) => !prev);
  }

  const drawerContent = (
    <Box sx={{ width: DRAWER_WIDTH }}>
      <Toolbar />
      <NavList items={protectedRoutes} />
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* ─── Top bar ───────────────────────────────────────────────── */}
      <AppBar position="fixed" sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
        <Toolbar>
          {!isDesktop && (
            <IconButton
              color="inherit"
              aria-label="open navigation"
              edge="start"
              onClick={handleDrawerToggle}
              sx={{ mr: 2 }}
            >
              <MenuIcon />
            </IconButton>
          )}
          <Typography variant="h6" noWrap component="div">
            Weigh to Go!
          </Typography>
          <Box sx={{ flexGrow: 1 }} />
          <UserMenu />
        </Toolbar>
      </AppBar>

      {/* ─── Side navigation ───────────────────────────────────────── */}
      {/* Mobile: temporary drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* Desktop: permanent drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          // Reserve horizontal space in the flex row. The drawer paper is
          // position:fixed (out of flow), so the docked root must carry the
          // width; without it the main region slides under the drawer (#130).
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
        }}
        open
      >
        {drawerContent}
      </Drawer>

      {/* ─── Page content ──────────────────────────────────────────── */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          // Let the flex item shrink below its content's intrinsic min-width
          // (the default is min-width:auto) so wide or unbreakable content does
          // not push the row past the viewport now that the drawer column is
          // reserved (#130).
          minWidth: 0,
          p: 3,
        }}
      >
        <Toolbar />
        {/* Each page slot is wrapped in an error boundary (SRS §10.2) and a
            Suspense boundary so a lazy page chunk loads — or fails — without
            unmounting the surrounding shell. Keyed by pathname so the boundary
            resets on navigation; otherwise a caught error would persist and
            strand the user on the fallback until a full refresh. */}
        <ErrorBoundary key={location.pathname}>
          <Suspense fallback={<LoadingSplash minHeight="60vh" />}>
            <Outlet />
          </Suspense>
        </ErrorBoundary>
      </Box>
    </Box>
  );
}
