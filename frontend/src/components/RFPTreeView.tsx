import * as React from 'react';
import Box from '@mui/material/Box';
import { RichTreeView } from '@mui/x-tree-view/RichTreeView';
import { TreeItem } from '@mui/x-tree-view/TreeItem';
import { Typography, CircularProgress } from '@mui/material';
import AddBoxIcon from '@mui/icons-material/AddBox';
import IndeterminateCheckBoxIcon from '@mui/icons-material/IndeterminateCheckBox';

import { styled } from '@mui/material/styles';
import { treeItemClasses } from '@mui/x-tree-view/TreeItem';
import SvgIcon, { SvgIconProps } from '@mui/material/SvgIcon';
import Button from '@mui/material/Button';

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

const CustomTreeItem = styled(TreeItem)({
  [`& .${treeItemClasses.iconContainer}`]: {
    '& .close': {
      opacity: 0.3,
    },
  },
});

function CloseSquare(props: SvgIconProps) {
  return (
    <SvgIcon
      className="close"
      fontSize="inherit"
      style={{ width: 14, height: 14 }}
      {...props}
    >
      {/* tslint:disable-next-line: max-line-length */}
      <path d="M17.485 17.512q-.281.281-.682.281t-.696-.268l-4.12-4.147-4.12 4.147q-.294.268-.696.268t-.682-.281-.281-.682.294-.669l4.12-4.147-4.12-4.147q-.294-.268-.294-.669t.281-.682.682-.281.696 .268l4.12 4.147 4.12-4.147q.294-.268.696-.268t.682.281 .281.669-.294.682l-4.12 4.147 4.12 4.147q.294.268 .294.669t-.281.682zM22.047 22.074v0 0-20.147 0h-20.12v0 20.147 0h20.12zM22.047 24h-20.12q-.803 0-1.365-.562t-.562-1.365v-20.147q0-.776.562-1.351t1.365-.575h20.147q.776 0 1.351.575t.575 1.351v20.147q0 .803-.575 1.365t-1.378.562v0z" />
    </SvgIcon>
  );
}

const RFPTreeView: React.FC<RFPTreeViewProps> = ({ rfpModel, selectedNodeId, onNodeSelect }) => {
  const [treeData, setTreeData] = React.useState<RFPNode[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [expandedItems, setExpandedItems] = React.useState<string[]>([]);

  const getAllItemsWithChildrenItemIds = React.useCallback(() => {
    const itemIds: string[] = [];
    const traverse = (nodes: RFPNode[]) => {
      nodes.forEach((node) => {
        itemIds.push(node.id);
        if (node.children) {
          traverse(node.children);
        }
      });
    };
    traverse(treeData);
    return itemIds;
  }, [treeData]);

  React.useEffect(() => {
    if (rfpModel?.rfpId) {
      setExpandedItems(getAllItemsWithChildrenItemIds());
    }
  }, [rfpModel?.rfpId, getAllItemsWithChildrenItemIds]);

  const handleExpandedItemsChange = (
    event: React.SyntheticEvent | null,
    itemIds: string[],
  ) => {
    setExpandedItems(itemIds);
  };

  const handleExpandClick = () => {
    setExpandedItems((oldExpanded) =>
      oldExpanded.length === 0 ? getAllItemsWithChildrenItemIds() : [],
    );
  };



  React.useEffect(() => {
    if (!rfpModel) {
      setLoading(false);
      return;
    }

    const buildTree = (): RFPNode[] => {
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
          summaryNode.children = rfpModel.rfpSummary.epics.map((epic: any, index: number) => {
            const epicNode: RFPNode = {
              id: epic.epicId,
              name: `Epic ${index + 1}`,
              type: 'epic',
              children: []
            };

            // Add Features to Epic
            if (epic.features?.length) {
              epicNode.children = epic.features.map((feature: any, index: number) => ({
                id: feature.featureId,
                name: `Feature ${index + 1}`,
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
        <Typography variant="subtitle1" color="text.secondary"
          onClick={() => onNodeSelect('resourceMix', 'resourceMix')}
        >Resource Mix</Typography>
      </Box>
      <Box sx={{ p: 1, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Typography variant="subtitle1" color="text.secondary">
          RFP Hierarchy
          <Button onClick={handleExpandClick} size="small" sx={{ float: 'right' }}>
            {expandedItems.length === 0 ? 'Expand' : 'Collapse'}
          </Button>
        </Typography>
      </Box>
      <Box sx={{ flex: 1, overflow: 'auto', p: 1, borderBottom: '1px solid', borderColor: 'divider' }}>
        <RichTreeView
          items={treeData}
          getItemLabel={(item) => item.name}
          expandedItems={expandedItems}
          onExpandedItemsChange={handleExpandedItemsChange}
          onItemSelectionToggle={(event, itemId) => {
            if (itemId) {
              const selectedNode = findNodeById(treeData, itemId);
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
            ),
            expandIcon: AddBoxIcon,
            collapseIcon: IndeterminateCheckBoxIcon,
            endIcon: CloseSquare
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