import React from 'react';
import { 
  AppBar, 
  Box, 
  CssBaseline, 
  Toolbar, 
  Typography, 
  useTheme,
  Container,
  styled
} from '@mui/material';

const StyledAppBar = styled(AppBar)(({ theme }) => ({
  background: 'white',
  color: theme.palette.text.primary,
  boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

const MainContent = styled('main')(({ theme }) => ({
  flexGrow: 1,
  padding: theme.spacing(3),
  backgroundColor: theme.palette.grey[50],
  minHeight: '100vh',
  width: '150%',
  [theme.breakpoints.up('lg')]: {
    padding: theme.spacing(4, 6),
  },
}));

interface LayoutProps {
  children: React.ReactNode;
  onAddRfp?: () => void;
}

export default function Layout({ children, onAddRfp }: LayoutProps) {
  const theme = useTheme();

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <CssBaseline />
      <StyledAppBar position="fixed">
        <Toolbar 
          sx={{ 
            px: { xs: 2, md: 4 },
            minHeight: 80,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <Box display="flex" alignItems="center">
            <Typography 
              variant="h4" 
              component="h1"
              sx={{
                fontWeight: 700,
                background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                letterSpacing: '-0.5px',
              }}
            >
              RFP Estimator Pro
            </Typography>
          </Box>
        </Toolbar>
      </StyledAppBar>
      
      <MainContent>
        <Toolbar sx={{ minHeight: '35px !important' }} />
        <Container maxWidth="xl" sx={{ height: '100%' }}>
          {children}
        </Container>
      </MainContent>
    </Box>
  );
}
