import { useParams } from 'react-router-dom';
import {
  Box,
  Paper,
  Button,
  styled,
  TextField,
  Typography,
  TableBody,
  Table,
  TableRow,
  TableCell,
  MenuItem,
  Chip,
  TableHead,
} from '@mui/material';
import RFPTreeView from '../components/RFPTreeView';
import RFPModel, { Allocation, Resource } from '../models/RFPModel';
import { useEffect, useState } from 'react';
import { Snackbar } from '@mui/material';
import Select from '@mui/material/Select'
import AllocationDialog from '../components/AllocationDialog';

// Styled Components
const LeftPane = styled(Paper)(({ theme }) => ({
  height: '100%',
  padding: theme.spacing(3),
  borderRadius: 12,
  boxShadow: '0 4px 20px 0 rgba(0,0,0,0.05)',
  backgroundColor: theme.palette.background.paper,
  overflow: 'hidden',
}));

const RightPane = styled(Paper)(({ theme }) => ({
  height: '100%', // Adjusted for padding
  padding: theme.spacing(2), // Reduced padding to allow more space for content
  borderRadius: 12,
  boxShadow: '0 4px 20px 0 rgba(0,0,0,0.05)',
  backgroundColor: theme.palette.background.paper,
  display: 'flex',
  flexDirection: 'column',
  overflow: 'hidden', // Changed from 'auto' to 'hidden' to prevent double scroll
}));

const RFP = () => {
  const { rfpId } = useParams<{ rfpId: string }>();
  const [rfp, setRfp] = useState<RFPModel | null>(null);
  const [input, setInput] = useState('');
  const [selectedNode, setSelectedNode] = useState<{ id: string; type: string } | null>(null);
  const [snackbar, setSnackbar] = useState({ open: false, message: '' });
  const [allocationDialogOpen, setAllocationDialogOpen] = useState(false);
  const [selectedResource, setSelectedResource] = useState<Resource | null>(null);


  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };
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
    setSelectedNode({ id: rfpId ? rfpId : '', type: 'rfp' });
  }, [rfpId]);

  const handleNodeSelect = (nodeId: string, nodeType: string) => {
    setSelectedNode({ id: nodeId, type: nodeType });
  };

  const handleOpenAllocationDialog = (resource: Resource) => {
    setSelectedResource(resource);
    setAllocationDialogOpen(true);
  };

  const handleSaveAllocations = (resourceId: string, updatedAllocations: Allocation[]) => {
    if (!rfp) return;

    const updatedResources = (rfp.resources || []).map(resource => {
      if (resource.resourceId === resourceId) {
        return {
          ...resource,
          allocations: updatedAllocations
        };
      }
      return resource;
    });

    setRfp({
      ...rfp,
      resources: updatedResources
    });
  };

  const save = async () => {
    try {
      let formData = '';
      let url = '';
      if (selectedNode?.type === 'resourceMix') {
        url = `/api/resources/save/${rfpId}`;
        formData = JSON.stringify(rfp);
      } else if (selectedNode?.type === 'summary') {
        url = `/api/summary/save/${selectedNode.id}`;
        formData = JSON.stringify(rfp?.rfpSummary);
      } else if (selectedNode?.type === 'epic') {
        url = `/api/epic/save/${selectedNode.id}`;
        formData = JSON.stringify(rfp?.rfpSummary?.epics?.find(e => e.epicId === selectedNode.id));
      } else if (selectedNode?.type === 'feature') {
        url = `/api/feature/save/${selectedNode.id}`;
        const epic = rfp?.rfpSummary?.epics?.find(e => e.features?.find(e => e.featureId === selectedNode.id));
        formData = JSON.stringify(epic?.features?.find(e => e.featureId === selectedNode.id));
      }

      const response = await fetch(url, {
        method: 'POST',
        body: formData,
        headers: {
          'Content-Type': 'application/json',
        },
      });
      if (!response.ok) throw new Error('Failed to save RFP');
      setSnackbar({ open: true, message: 'Saved successfully!' });
    } catch (error) {
      console.error('Error saving RFP:', error);
      setSnackbar({ open: true, message: 'Failed to save. Please try again.' });
    }
  };

  const deleteItem = async () => {
    try {
      let url = '';
      if (selectedNode?.type === 'epic') {
        url = `/api/epic/delete/${selectedNode.id}`;
      } else if (selectedNode?.type === 'feature') {
        url = `/api/feature/delete/${selectedNode.id}`;
      }

      const response = await fetch(url, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      if (!response.ok) throw new Error('Failed to delete RFP');
      setSnackbar({ open: true, message: 'Deleted successfully!' });
      window.location.reload();  // Reload the page to show updated data
    } catch (error) {
      console.error('Error deleting RFP:', error);
      setSnackbar({ open: true, message: 'Failed to delete. Please try again.' });
    }
  };

  const addItem = async () => {
    try {
      let url = '';
      if (selectedNode?.type === 'summary') {
        url = `/api/epic/add/${selectedNode.id}`;
      } else if (selectedNode?.type === 'epic') {
        url = `/api/feature/add/${selectedNode.id}`;
      }

      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      if (!response.ok) throw new Error('Failed to add RFP');
      setSnackbar({ open: true, message: 'Added successfully!' });
      window.location.reload();  // Reload the page to show updated data
    } catch (error) {
      console.error('Error adding RFP:', error);
      setSnackbar({ open: true, message: 'Failed to add. Please try again.' });
    }
  };

  const handleFormSubmit = async () => {
    if (window.confirm('Generate will override existing data and unsaved changes. Are you sure?')) {
      try {
        let url = `/api/rfp/analyze/${selectedNode?.id}`;

        if (selectedNode?.type === 'rfp') {
          url = `/api/rfp/analyze/${selectedNode.id}`;
        } else if (selectedNode?.type === 'summary') {
          url = `/api/solution/generate-epics/${selectedNode.id}`;
        } else if (selectedNode?.type === 'epic') {
          url = `/api/solution/generate-features/${selectedNode.id}`;
        } else if (selectedNode?.type === 'feature') {
          url = `/api/solution/generate-feature-estimates/${selectedNode.id}`;
        } else if (selectedNode?.type === 'resourceMix') {
          url = `/api/solution/generate-resource-mix/${rfpId}`;
        }
        const response = await fetch(url, {
          method: 'POST',
          body: JSON.stringify({ prompt: input }),
          headers: {
            'Content-Type': 'application/json',
          },
        });
        if (!response.ok) throw new Error('Failed to analyze RFP');
        setSnackbar({ open: true, message: 'Operation completed successfully!' });
        window.location.reload();  // Reload the page to show updated data
      } catch (error) {
        console.error('Error analyzing RFP:', error);
        setSnackbar({ open: true, message: 'Failed to complete the operation. Please try again.' });
      }
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
      case 'resourceMix':
        return (
          <Table sx={{ width: '100%', height: '75%' }}>
            <TableBody>
              <TableRow>
                <TableCell align="left">
                  <Button variant="contained" color="primary" onClick={() => save()} size="small">
                    Save
                  </Button> &nbsp;
                  <Button variant="contained" color="primary" onClick={() => handleFormSubmit()} size="small">
                    Generate Resource Mix
                  </Button>
                </TableCell>
              </TableRow>
              <TableRow sx={{ height: '75%' }}>
                <TableCell>
                  <Table size="small" sx={{ minWidth: 350 }}>
                    <TableHead>
                      <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                        <TableCell sx={{ py: 0.5, fontWeight: 600, fontSize: "0.85rem" }}>
                          Resource Type
                        </TableCell>
                        <TableCell
                          align="center"
                          sx={{ py: 0.5, fontWeight: 600, fontSize: "0.85rem" }}
                        >
                          Count
                        </TableCell>
                        <TableCell
                          align="center"
                          sx={{ py: 0.5, fontWeight: 600, fontSize: "0.85rem" }}
                        > Action
                        </TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rfp?.resources?.map((resource: any) => (
                        <TableRow
                          key={resource.resourceId}
                          hover
                          sx={{
                            "& td": { py: 0.3, fontSize: "0.8rem" },
                            "&:nth-of-type(odd)": { backgroundColor: "#fafafa" }
                          }}
                        >
                          <TableCell>{resource.resourceType}</TableCell>
                          <TableCell align="center">{resource.allocations?.length}</TableCell>
                          <TableCell align="center">
                            <Button 
                              variant="contained" 
                              color="primary" 
                              onClick={() => handleOpenAllocationDialog(resource)} 
                              size="small"
                              sx={{
                                minWidth: 'auto',
                                padding: '2px 8px',
                                fontSize: '0.7rem',
                                lineHeight: 1.2,
                                height: '24px'
                              }}
                            >
                              Edit
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>

                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        )
      case 'rfp':
        return (
          <Table sx={{ width: '100%', height: '100%' }}>
            <TableBody>
              <TableRow>
                <TableCell align="center">
                  <Button variant="contained" color="primary" onClick={() => handleFormSubmit()} size="small">
                    Generate Summary
                  </Button>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );
      case 'summary':
        return (
          <Table sx={{ width: '100%', height: '100%' }}>
            <TableBody>
              <TableRow>
                <TableCell colSpan={2}>
                  <Button variant="contained" color="primary" onClick={() => save()} size="small">
                    Save
                  </Button>
                  &nbsp;
                  <Button variant="contained" color="primary" onClick={() => addItem()} size="small">
                    Add Epic
                  </Button>
                  &nbsp;
                  <Button variant="contained" color="primary" onClick={() => handleFormSubmit()} size="small">
                    Generate Epics
                  </Button>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ width: '60%' }}>
                  <TextField fullWidth multiline rows={7} label="Functional Requirements"
                    value={rfp?.rfpSummary?.functionalRequirements || ''}
                    onChange={(e) => {
                      if (!rfp) return;
                      setRfp({
                        ...rfp, rfpSummary: {
                          ...(rfp.rfpSummary || { summaryId: '', epics: [] }),
                          functionalRequirements: e.target.value
                        }
                      });
                    }}
                  />
                </TableCell>
                <TableCell rowSpan={2}>
                  <TextField fullWidth multiline rows={17} label="Summary"
                    value={rfp?.rfpSummary?.summary || ''}
                    onChange={(e) => {
                      if (!rfp) return;
                      setRfp({
                        ...rfp, rfpSummary: {
                          ...(rfp.rfpSummary || { summaryId: '', epics: [] }),
                          summary: e.target.value
                        }
                      });
                    }}
                  />
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell>
                  <TextField fullWidth multiline rows={7} label="Non Functional Requirements"
                    value={rfp?.rfpSummary?.nonFunctionalRequirements || ''}
                    onChange={(e) => {
                      if (!rfp) return;
                      setRfp({
                        ...rfp, rfpSummary: {
                          ...(rfp.rfpSummary || { summaryId: '', epics: [] }),
                          nonFunctionalRequirements: e.target.value
                        }
                      });
                    }}
                  />
                </TableCell>

              </TableRow>
            </TableBody>
          </Table>
        );
      case 'epic':
        const epic = rfp?.rfpSummary?.epics?.find(e => e.epicId === selectedNode.id);
        return (
          <Table>
            <TableBody>
              <TableRow>
                <TableCell colSpan={2}  >
                  <Button variant="contained" color="primary" onClick={() => save()} size="small">
                    Save
                  </Button>
                  &nbsp;
                  <Button variant="contained" color="primary" onClick={() => deleteItem()} size="small">
                    Delete
                  </Button>
                  &nbsp;
                  <Button variant="contained" color="primary" onClick={() => addItem()} size="small">
                    Add Feature
                  </Button>
                  &nbsp;
                  <Button variant="contained" color="primary" onClick={() => handleFormSubmit()} size="small">
                    Generate Features
                  </Button>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell>
                  <TextField fullWidth multiline rows={18} label="Epic Description"
                    value={epic?.epicDescription || 'No description available'}
                    onChange={(e) => {
                      if (!rfp) return;
                      setRfp({
                        ...rfp, rfpSummary: {
                          ...(rfp.rfpSummary || { summaryId: '', epics: [] }),
                          epics: rfp?.rfpSummary?.epics?.map(epic => {
                            if (epic.epicId === selectedNode.id) {
                              return {
                                ...epic,
                                epicDescription: e.target.value
                              };
                            }
                            return epic;
                          })
                        }
                      });
                    }}
                  />
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );
      case 'feature':
        const allFeatures = rfp?.rfpSummary?.epics?.flatMap(epic =>
          epic.features?.map(feature => ({ ...feature, epicId: epic.epicId })) || []
        ) || [];
        const feature = allFeatures.find(f => f.featureId === selectedNode.id);
        return (
          <Table>
            <TableBody>
              <TableRow>
                <TableCell colSpan={3}>
                  <Button variant="contained" color="primary" onClick={() => save()} size="small">
                    Save
                  </Button>
                  &nbsp;
                  <Button variant="contained" color="primary" onClick={() => deleteItem()} size="small">
                    Delete
                  </Button>
                  &nbsp;
                  <Button variant="contained" color="primary" onClick={() => handleFormSubmit()} size="small">
                    Generate Feature Estimates
                  </Button>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ width: '60%' }}>
                  <TextField fullWidth multiline rows={18} label="Feature Description"
                    value={feature?.featureDescription || 'No description available'}
                    onChange={(e) => {
                      if (!rfp) return;
                      setRfp({
                        ...rfp, rfpSummary: {
                          ...(rfp.rfpSummary || { summaryId: '', epics: [] }),
                          epics: rfp?.rfpSummary?.epics?.map(epic => {
                            return {
                              ...epic,
                              features: epic?.features?.map(feature => {
                                if (feature.featureId === selectedNode.id) {
                                  return {
                                    ...feature,
                                    featureDescription: e.target.value
                                  };
                                }
                                return feature;
                              })
                            };
                          })
                        }
                      });
                    }}
                  />
                </TableCell>
                <TableCell>
                  <Table>
                    <TableBody>
                      <TableRow>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'right', gap: 1, padding: 0.25 }}>
                            <Chip label="Effort Size" />
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Select value={feature?.effortSize} size="small"
                            onChange={(e) => {
                              if (!rfp) return;
                              setRfp({
                                ...rfp, rfpSummary: {
                                  ...(rfp.rfpSummary || { summaryId: '', epics: [] }),
                                  epics: rfp?.rfpSummary?.epics?.map(epic => {
                                    return {
                                      ...epic,
                                      features: epic?.features?.map(feature => {
                                        if (feature.featureId === selectedNode.id) {
                                          return {
                                            ...feature,
                                            effortSize: e.target.value
                                          };
                                        }
                                        return feature;
                                      })
                                    };
                                  })
                                }
                              });
                            }}
                          >
                            <MenuItem value="XS">XS</MenuItem>
                            <MenuItem value="S">S</MenuItem>
                            <MenuItem value="M">M</MenuItem>
                            <MenuItem value="L">L</MenuItem>
                            <MenuItem value="XL">XL</MenuItem>
                            <MenuItem value="XXL">XXL</MenuItem>
                          </Select>
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'right', gap: 1, padding: 0.25 }}>
                            <Chip label="Integration Buffer (%)" />
                          </Box>
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number" size="small"
                            slotProps={{
                              htmlInput: {
                                min: 0, // Minimum allowed value
                                max: 100, // Maximum allowed value
                              },
                            }}
                            value={feature?.integrationBuffer}
                            onChange={(e) => {
                              if (!rfp) return;
                              setRfp({
                                ...rfp, rfpSummary: {
                                  ...(rfp.rfpSummary || { summaryId: '', epics: [] }),
                                  epics: rfp?.rfpSummary?.epics?.map(epic => {
                                    return {
                                      ...epic,
                                      features: epic?.features?.map(feature => {
                                        if (feature.featureId === selectedNode.id) {
                                          return {
                                            ...feature,
                                            integrationBuffer: Number(e.target.value)
                                          };
                                        }
                                        return feature;
                                      })
                                    };
                                  })
                                }
                              });
                            }} />
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'right', gap: 1, padding: 0.25 }}>
                            <Chip label="Requirements Clarity Buffer (%)" />
                          </Box>
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number" size="small"
                            slotProps={{
                              htmlInput: {
                                min: 0, // Minimum allowed value
                                max: 100, // Maximum allowed value
                              },
                            }}
                            value={feature?.requirementsClarityBuffer}
                            onChange={(e) => {
                              if (!rfp) return;
                              setRfp({
                                ...rfp, rfpSummary: {
                                  ...(rfp.rfpSummary || { summaryId: '', epics: [] }),
                                  epics: rfp?.rfpSummary?.epics?.map(epic => {
                                    return {
                                      ...epic,
                                      features: epic?.features?.map(feature => {
                                        if (feature.featureId === selectedNode.id) {
                                          return {
                                            ...feature,
                                            requirementsClarityBuffer: Number(e.target.value)
                                          };
                                        }
                                        return feature;
                                      })
                                    };
                                  })
                                }
                              });
                            }} />
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'right', gap: 1, padding: 0.25 }}>
                            <Chip label="Complexity Buffer (%))" />
                          </Box>
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number" size="small"
                            slotProps={{
                              htmlInput: {
                                min: 0, // Minimum allowed value
                                max: 100, // Maximum allowed value
                              },
                            }}
                            value={feature?.complexityBuffer}
                            onChange={(e) => {
                              if (!rfp) return;
                              setRfp({
                                ...rfp, rfpSummary: {
                                  ...(rfp.rfpSummary || { summaryId: '', epics: [] }),
                                  epics: rfp?.rfpSummary?.epics?.map(epic => {
                                    return {
                                      ...epic,
                                      features: epic?.features?.map(feature => {
                                        if (feature.featureId === selectedNode.id) {
                                          return {
                                            ...feature,
                                            complexityBuffer: Number(e.target.value)
                                          };
                                        }
                                        return feature;
                                      })
                                    };
                                  })
                                }
                              });
                            }} />
                        </TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        );
      default:
        return null;
    }
  }

  return (
    <Box sx={{
      display: 'grid',
      gridTemplateColumns: '300px 1fr',
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
        {renderNodeContent()}
        <Box sx={{ paddingTop: 2, marginTop: 'auto', width: '100%' }}>
          <TextField
            fullWidth
            id="outlined-multiline-static"
            label="Additional Prompt"
            multiline
            rows={5}
            defaultValue={input}
            onChange={(e) => setInput(e.target.value)}
          />
        </Box>
      </RightPane>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        message={snackbar.message}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      />

      <AllocationDialog
        open={allocationDialogOpen}
        resource={selectedResource}
        onClose={() => setAllocationDialogOpen(false)}
        onSave={handleSaveAllocations}
      />
    </Box>
  );
};

export default RFP;