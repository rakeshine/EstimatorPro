import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  Chip,
  CircularProgress,
  InputAdornment,
  styled,
  alpha,
} from '@mui/material';
import { CloudUpload as UploadIcon, Search as SearchIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

// Styled Components
const StyledTableContainer = styled(TableContainer)(({ theme }) => ({
  marginTop: theme.spacing(2),
  borderRadius: 8,
  boxShadow: '0 4px 20px 0 rgba(0,0,0,0.05)',
  '& .MuiTableCell-root': {
    padding: theme.spacing(1.25, 2),
  },
  '& .MuiTableHead-root': {
    backgroundColor: alpha('#1976d2', 0.06),
  },
}));

const UploadButton = styled('label')(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  gap: theme.spacing(1),
  padding: theme.spacing(1, 2),
  borderRadius: 6,
  backgroundColor: '#1976d2',
  color: '#fff',
  cursor: 'pointer',
  fontWeight: 600,
  '&:hover': {
    backgroundColor: '#125ea8',
  },
  '& input': {
    display: 'none',
  },
}));

interface RFP {
  rfpId: string;
  originalFilename: string;
  status?: string;
  createDateTime?: string;
  description?: string;
}

const Home = () => {
  const navigate = useNavigate();
  const [rfps, setRfps] = useState<RFP[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [orderBy, setOrderBy] = useState<keyof RFP>('createDateTime');
  const [order, setOrder] = useState<'asc' | 'desc'>('desc');
  const [searchTerm, setSearchTerm] = useState('');
  const [isUploading, setIsUploading] = useState(false);

  useEffect(() => {
    const fetchRFPs = async () => {
      try {
        setLoading(true);
        const response = await fetch('/api/rfps');
        if (!response.ok) throw new Error('Failed to fetch RFPs');
        const data = await response.json();
        setRfps(data);
      } catch (error) {
        console.error('Error fetching RFPs:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchRFPs();
  }, []);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 0) {
      handleUpload(event.target.files[0]);
    }
  };

  const handleUpload = async (fileToUpload: File) => {
    if (!fileToUpload) return;
    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append('rfpFile', fileToUpload);

      const response = await fetch('/api/rfp/upload', {
        method: 'POST',
        body: formData,
      });
      if (!response.ok) throw new Error('Upload failed');
      await response.json();
      // refresh list
      const updated = await fetch('/api/rfps');
      const updatedData = await updated.json();
      setRfps(updatedData);
    } catch (error) {
      console.error('Upload failed:', error);
    } finally {
      setIsUploading(false);
    }
  };

  const handleDelete = async (rfpId: string) => {
    if (window.confirm('Are you sure you want to delete this RFP? This action cannot be undone.')) {
      try {
        const response = await fetch(`/api/rfp/${rfpId}`, {
          method: 'DELETE',
        });
        if (!response.ok) throw new Error('Failed to delete RFP');
        await response.json();
        // refresh list
        const updated = await fetch('/api/rfps');
        const updatedData = await updated.json();
        setRfps(updatedData);
      } catch (error) {
        console.error('Error deleting RFP:', error);
      }
    }
  };

  const handleRequestSort = (property: keyof RFP) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const handleChangePage = (_: unknown, newPage: number) => setPage(newPage);
  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 5));
    setPage(0);
  };

  const filtered = rfps.filter((r) =>
    [r.originalFilename, r.status, r.description]
      .filter(Boolean)
      .some((v) => String(v).toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const sorted = [...filtered].sort((a, b) => {
    const aVal = (a[orderBy] ?? '') as any;
    const bVal = (b[orderBy] ?? '') as any;
    if (aVal === bVal) return 0;
    return order === 'asc' ? (aVal < bVal ? -1 : 1) : (aVal > bVal ? -1 : 1);
  });

  const pageItems = sorted.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const statusColor = (status?: string) => {
    switch (status) {
      case 'In Progress':
        return 'primary';
      case 'Completed':
        return 'success';
      case 'Archived':
        return 'secondary';
      default:
        return 'default';
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <UploadButton>
          <UploadIcon />
          <span>Upload RFP</span>
          <input type="file" accept=".pdf,.doc,.docx" onChange={handleFileChange} disabled={isUploading} />
        </UploadButton>
        <TextField
          size="small"
          placeholder="Filter RFPs..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            },
          }}
        />
      </Box>

      <StyledTableContainer>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <TableSortLabel
                      active={orderBy === 'originalFilename'}
                      direction={orderBy === 'originalFilename' ? order : 'asc'}
                      onClick={() => handleRequestSort('originalFilename')}
                    >
                      Name
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>
                    <TableSortLabel
                      active={orderBy === 'status'}
                      direction={orderBy === 'status' ? order : 'asc'}
                      onClick={() => handleRequestSort('status')}
                    >
                      Status
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>
                    <TableSortLabel
                      active={orderBy === 'createDateTime'}
                      direction={orderBy === 'createDateTime' ? order : 'desc'}
                      onClick={() => handleRequestSort('createDateTime')}
                    >
                      Created
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>
                    Actions
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {pageItems.map((rfp) => (
                  <TableRow key={rfp.rfpId} hover>
                    <TableCell>
                      <Typography variant="subtitle2">{rfp.originalFilename}</Typography>
                      {rfp.description && (
                        <Typography variant="body2" color="text.secondary" noWrap>
                          {rfp.description}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Chip size="small" label={rfp.status || 'Draft'} color={statusColor(rfp.status) as any} />
                    </TableCell>
                    <TableCell>
                      {rfp.createDateTime ? new Date(rfp.createDateTime).toLocaleDateString() : '-'}
                    </TableCell>
                    <TableCell>
                      <Button
                        variant="contained"
                        color="primary"
                        onClick={() => navigate(`/rfp/${rfp.rfpId}`)}
                        size="small"
                      >
                        Edit
                      </Button> &nbsp;
                      <Button
                        variant="contained"
                        color="primary"
                        onClick={() => handleDelete(rfp.rfpId)}
                        size="small"
                      >
                        Delete
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <TablePagination
              rowsPerPageOptions={[5]}
              component="div"
              count={sorted.length}
              rowsPerPage={rowsPerPage}
              page={page}
              onPageChange={handleChangePage}
              onRowsPerPageChange={handleChangeRowsPerPage}
            />
          </>
        )}
      </StyledTableContainer>
    </Box>
  );
};

export default Home;