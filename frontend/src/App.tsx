import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Home from './pages/Home';
import RFP from './pages/RFP';
import Layout from './components/Layout';
import { styled } from '@mui/material/styles';
import { Box } from '@mui/material';

const AppContainer = styled(Box)({
  width: '100vw',
  height: '100vh',
  overflow: 'hidden', // This will prevent scrolling at the root level
  display: 'flex',
  flexDirection: 'column',
});

const theme = createTheme({
  palette: {
    primary: {
      main: '#2563eb',
    },
    secondary: {
      main: '#64748b',
    },
    background: {
      default: '#f8fafc',
      paper: '#ffffff',
    },
  },
  shape: {
    borderRadius: 12,
  }
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AppContainer>
        <Router>
          <Layout>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/rfp/:rfpId" element={<RFP />} />
            </Routes>
          </Layout>
        </Router>
      </AppContainer>
    </ThemeProvider>
  );
}

export default App;
