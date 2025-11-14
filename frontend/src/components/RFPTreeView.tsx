import * as React from 'react';
import Box from '@mui/material/Box';
import { RichTreeView } from '@mui/x-tree-view/RichTreeView';
import { TreeItem, TreeItemProps } from '@mui/x-tree-view/TreeItem';
import { Typography, CircularProgress } from '@mui/material';
import DocumentIcon from '@mui/icons-material/DescriptionOutlined';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import FolderIcon from '@mui/icons-material/Folder';
import CategoryIcon from '@mui/icons-material/Category';

interface RFPNode {
  id: string;
  name: string;
  type: 'rfp' | 'summary' | 'epic' | 'feature';
  children?: RFPNode[];
}

interface RFPTreeViewProps {
  rfpModel: any | null;
  selectedNodeId?: string | null;
  onNodeSelect: (nodeId: string, nodeType: string) => void;
}

const CustomTreeItem = React.forwardRef(function CustomTreeItem(
  props: TreeItemProps & {
    nodeType?: string;
    selectedNodeId?: string | null;
    item?: RFPNode;  // Add this line
  },
  ref: React.Ref<HTMLLIElement>,
) {
  const { nodeType, selectedNodeId, itemId, item, ...other } = props;
  const isSelected = selectedNodeId === itemId;

  const getIcon = () => {
    const iconStyle = { mr: 1, fontSize: 18 };
    const type = nodeType || item?.type;
    switch (type) {
      case 'rfp': return <DocumentIcon color="primary" sx={iconStyle} />;
      case 'summary': return <DescriptionOutlinedIcon color="secondary" sx={iconStyle} />;
      case 'epic': return <FolderIcon color="warning" sx={iconStyle} />;
      case 'feature': return <CategoryIcon color="success" sx={iconStyle} />;
      default: return null;
    }
  };

  return (
    <TreeItem
      ref={ref}
      {...other}
      itemId={itemId}
      label={
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            backgroundColor: isSelected ? 'rgba(25, 118, 210, 0.08)' : 'transparent',
            p: 0.5,
            borderRadius: 1,
            '&:hover': {
              backgroundColor: 'rgba(0, 0, 0, 0.04)',
            },
          }}
        >
          {getIcon()}
          <Typography variant="body2">{props.label}</Typography>
        </Box>
      }
    />
  );
});

const RFPTreeView: React.FC<RFPTreeViewProps> = ({ rfpModel, selectedNodeId, onNodeSelect }) => {
  const [treeData, setTreeData] = React.useState<RFPNode[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [expandedItems, setExpandedItems] = React.useState<string[]>([]);

  React.useEffect(() => {
    if (rfpModel?.rfpId) {
      setExpandedItems([rfpModel.rfpId]);
    }
  }, [rfpModel?.rfpId]);

  const handleItemExpansionToggle = (event: React.SyntheticEvent, itemIds: string[]) => {
    setExpandedItems(itemIds);
  };

  React.useEffect(() => {
    if (!rfpModel) {
      setLoading(false);
      return;
    }

    const buildTree = (): RFPNode[] => {
      const nodes: RFPNode[] = [];

      // Add RFP node
      const rfpNode: RFPNode = {
        id: rfpModel.rfpId,
        name: rfpModel.originalFilename,
        type: 'rfp',
        children: []
      };

      // Add Summary node if exists
      if (rfpModel.rfpSummary) {
        const summaryNode: RFPNode = {
          id: rfpModel.rfpSummary.summaryId,
          name: 'Summary',
          type: 'summary',
          children: []
        };

        // Add Epics and Features
        if (rfpModel.rfpSummary.epics?.length) {
          summaryNode.children = rfpModel.rfpSummary.epics.map((epic: any) => {
            const epicNode: RFPNode = {
              id: epic.epicId,
              name: epic.epicDescription || `Epic ${epic.epicId}`,
              type: 'epic',
              children: []
            };

            // Add Features to Epic
            if (epic.features?.length) {
              epicNode.children = epic.features.map((feature: any) => ({
                id: feature.featureId,
                name: feature.featureDescription || `Feature ${feature.featureId}`,
                type: 'feature'
              }));
            }

            return epicNode;
          });
        }

        rfpNode.children?.push(summaryNode);
      }

      return [rfpNode];
    };

    setTreeData(buildTree());
    setLoading(false);
  }, [rfpModel]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
        <CircularProgress size={24} />
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ p: 1, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Typography variant="subtitle2" color="text.secondary">
          RFP Hierarchy
        </Typography>
      </Box>
      <Box sx={{ flex: 1, overflow: 'auto', p: 1 }}>
        <RichTreeView
          items={treeData}
          getItemLabel={(item) => item.name}
          expandedItems={expandedItems}
          onExpandedItemsChange={handleItemExpansionToggle}
          onItemSelectionToggle={(event, itemIds) => {
            if (itemIds.length > 0) {
              const selectedNode = findNodeById(treeData, itemIds[0]);
              if (selectedNode) {
                onNodeSelect(selectedNode.id, selectedNode.type);
              }
            }
          }}
          slots={{
            item: (props: any) => (
              <CustomTreeItem
                {...props}
                nodeType={props.item?.type}
                selectedNodeId={selectedNodeId}
                item={props.item}
              />
            )
          }}
          sx={{
            '--TreeView-spacing': '4px',
            '--TreeView-indentation': '24px',
          }}
        />
      </Box>
    </Box>
  );
};

// Helper function to find a node by ID
function findNodeById(nodes: RFPNode[], id: string): RFPNode | null {
  for (const node of nodes) {
    if (node.id === id) return node;
    if (node.children) {
      const found = findNodeById(node.children, id);
      if (found) return found;
    }
  }
  return null;
}

export default RFPTreeView;