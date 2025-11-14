import { useParams } from 'react-router-dom';
import { Col, Row } from 'react-bootstrap';
import {
  Box,
  Paper,
  Button,
  styled,
  TextField,
  Typography,
  IconButton,
  InputAdornment,
  Container,
  Card,
  CardContent,
  CardActions,
  Grid,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import RFPTreeView from '../components/RFPTreeView';
import RFPModel from '../models/RFPModel';
import { useEffect, useState } from 'react';

// Styled Components
const LeftPane = styled(Paper)(({ theme }) => ({
  height: '95%',
  padding: theme.spacing(3),
  borderRadius: 12,
  boxShadow: '0 4px 20px 0 rgba(0,0,0,0.05)',
  backgroundColor: theme.palette.background.paper,
  overflow: 'hidden',
}));

const RightPane = styled(Paper)(({ theme }) => ({
  height: '95%', // Adjusted for padding
  padding: theme.spacing(2), // Reduced padding to allow more space for content
  borderRadius: 12,
  boxShadow: '0 4px 20px 0 rgba(0,0,0,0.05)',
  backgroundColor: theme.palette.background.paper,
  display: 'flex',
  flexDirection: 'column',
  overflow: 'hidden', // Changed from 'auto' to 'hidden' to prevent double scroll
}));

const TopPane = styled(Box)(({ theme }) => ({
  flex: '0 0 72%',
  overflowY: 'auto',
  padding: theme.spacing(2),
  borderBottom: `0.5px solid ${theme.palette.divider}`,
}));

const BottomPane = styled(Box)(({ theme }) => ({
  flex: '0 0 28%',
  padding: theme.spacing(2),
  paddingTop: theme.spacing(3),
  display: 'flex',
  flexDirection: 'column',
  minHeight: 0, // Important for proper scrolling
}));

const RFP = () => {
  const { rfpId } = useParams<{ rfpId: string }>();
  const [rfp, setRfp] = useState<RFPModel | null>(null);
  const [input, setInput] = useState('');
  const [selectedNode, setSelectedNode] = useState<{ id: string; type: string } | null>(null);

  useEffect(() => {
    const fetchRFP = async () => {
      try {
        const response = await fetch(`/api/rfp/${rfpId}`);
        if (!response.ok) throw new Error('Failed to fetch RFP');
        const data = await response.json();
        setRfp(data);
      } catch (error) {
        console.error('Error fetching RFPs:', error);
      }
    };
    fetchRFP();
  }, [rfpId]);

  const handleNodeSelect = (nodeId: string, nodeType: string) => {
    setSelectedNode({ id: nodeId, type: nodeType });
  };

  const handleFormSubmit = async () => {
    try {
      const formData = new FormData();
      formData.append('prompt', input);
      const response = await fetch(`/api/rfp/analyze/${rfpId}`, {
        method: 'POST',
        body: formData,
      });
      if (!response.ok) throw new Error('Failed to analyze RFP');
      const data = await response.json();
      console.log(data);
    } catch (error) {
      console.error('Error analyzing RFP:', error);
    }
  };

  function renderNodeContent() {
    if (!selectedNode) {
      return (
        <Box sx={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100%',
          color: 'text.secondary'
        }}>
          <Typography variant="h6">Select a node to view details</Typography>
        </Box>
      );
    }

    switch (selectedNode.type) {
      case 'summary':
        return (
          <Box sx={{ mt: 2 }}>
            <TextField
              fullWidth
              multiline
              rows={16}
              label="Summary"
              value={rfp?.rfpSummary?.summary || ''}
            />
          </Box>
        );
      case 'epic':
        const epic = rfp?.rfpSummary?.epics?.find(e => e.epicId === selectedNode.id);
        return (
          <Box sx={{ mt: 2 }}>
            <TextField
              fullWidth
              multiline
              rows={16}
              label="Epic Description"
              value={epic?.epicDescription || 'No description available'}
            />
          </Box>
        );
      case 'feature':
        const allFeatures = rfp?.rfpSummary?.epics?.flatMap(epic => 
          epic.features?.map(feature => ({ ...feature, epicId: epic.epicId })) || []
        ) || [];
        const feature = allFeatures.find(f => f.featureId === selectedNode.id);
        return (
          <Box sx={{ mt: 2 }}>
            <TextField
              fullWidth
              multiline
              rows={16}
              label="Feature Description"
              value={feature?.featureDescription || 'No description available'}
            />
          </Box>
        );
      default:
        return null;
    }
  }

  return (
    <Box sx={{
      display: 'grid',
      gridTemplateColumns: '400px 1fr',
      gap: 2,
      p: 2,
      boxSizing: 'border-box',
      height: 'calc(100vh - 80px)', // Adjust based on your app bar height
      overflow: 'hidden'
    }}>
      <LeftPane elevation={0}>
        <RFPTreeView 
          rfpModel={rfp} 
          selectedNodeId={selectedNode?.id}
          onNodeSelect={handleNodeSelect} />
      </LeftPane>

      <RightPane elevation={0}>
        <TopPane>
          <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: 2, width: '100%' }}>
            <Grid container spacing={2} sx={{ width: '100%', justifyContent: 'space-between', alignItems: 'center' }}>
              <Grid>
                &nbsp;
              </Grid>
              <Grid>
                  <Button variant="contained" color="primary" onClick={() => handleFormSubmit()} size="small">
                    Submit
                  </Button>
              </Grid>
            </Grid>
          </Box>
          {renderNodeContent()}
        </TopPane>

        <BottomPane>
          <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
            <TextField
                  id="outlined-multiline-static"
                  label="Additional Prompt"
                  multiline
                  rows={5}
                  defaultValue={input}
                  onChange={(e) => setInput(e.target.value)}
                />
          </Box>
        </BottomPane>
      </RightPane>
    </Box>
  );
};

export default RFP;