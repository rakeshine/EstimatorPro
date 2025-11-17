import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Box
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { Allocation, Resource } from '../models/RFPModel';

interface AllocationDialogProps {
  open: boolean;
  resource: Resource | null;
  onClose: () => void;
  onSave: (resourceId: string, allocations: Allocation[]) => void;
}

const AllocationDialog: React.FC<AllocationDialogProps> = ({ 
  open, 
  resource, 
  onClose, 
  onSave 
}) => {
  const [allocations, setAllocations] = useState<Allocation[]>([]);

  useEffect(() => {
    if (resource) {
      setAllocations([...resource.allocations || []]);
    }
  }, [resource]);

  const handleAddAllocation = () => {
    setAllocations([...allocations, { 
      allocationId: `temp-${Date.now()}`, 
      allocationPercent: 100
    }]);
  };

  const handleRemoveAllocation = (id: string) => {
    setAllocations(allocations.filter(a => a.allocationId !== id));
  };

  const handleAllocationChange = (id: string, field: string, value: number) => {
    setAllocations(allocations.map(a => 
      a.allocationId === id ? { ...a, [field]: value } : a
    ));
  };

  const handleSave = () => {
    if (resource) {
      onSave(resource.resourceId, allocations);
    }
    onClose();
  };

  if (!resource) return null;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xs"
      fullWidth
    >
      <DialogTitle>
        Allocations for {resource.resourceType}
      </DialogTitle>
      <DialogContent>
        <Box sx={{ mt: 2, mb: 3 }}>
          <Button 
            variant="contained" 
            color="primary" 
            onClick={handleAddAllocation}
            startIcon={<AddIcon />}
          >
            Add Allocation
          </Button>
        </Box>
        
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell align="center">Allocation %</TableCell>
                <TableCell align="center" width={100}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {allocations.map((allocation) => (
                <TableRow key={allocation.allocationId}>
                  <TableCell align="center">
                    <TextField
                      type="number"
                      value={allocation.allocationPercent}
                      onChange={(e) => 
                        handleAllocationChange(
                          allocation.allocationId, 
                          'allocationPercent', 
                          Number(e.target.value)
                        )
                      }
                      inputProps={{ min: 0, max: 100, step: 1 }}
                      size="small"
                      fullWidth
                    />
                  </TableCell>
                  <TableCell>
                    <IconButton 
                      onClick={() => handleRemoveAllocation(allocation.allocationId)}
                      color="error"
                      size="small"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button 
          onClick={handleSave} 
          variant="contained" 
          color="primary"
        >
          Save Changes
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AllocationDialog;