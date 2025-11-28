'use client';

import { useState } from 'react';
import { Sparkles, FormInput, Table2, Loader2, Copy, Check, Download, Pencil, Trash2, Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { DynamicForm } from '@/components/ai/DynamicForm';
import { DynamicTable } from '@/components/ai/DynamicTable';
import { aiService } from '@/services/ai.service';
import { GeneratedFormSchema, GeneratedTableSchema } from '@/types/ai.types';
import { useLanguage } from '@/contexts/language-context';

// Sample data for table preview
const sampleTableData = [
  { id: 1, name: 'Item 1', status: 'Active', createdAt: '2024-01-15' },
  { id: 2, name: 'Item 2', status: 'Pending', createdAt: '2024-01-16' },
  { id: 3, name: 'Item 3', status: 'Completed', createdAt: '2024-01-17' },
];

export default function AiGeneratorPage() {
  const { t } = useLanguage();
  const [activeTab, setActiveTab] = useState('form');

  // Form generator state
  const [formPrompt, setFormPrompt] = useState('');
  const [formEntityType, setFormEntityType] = useState('');
  const [formLoading, setFormLoading] = useState(false);
  const [generatedForm, setGeneratedForm] = useState<GeneratedFormSchema | null>(null);
  const [formCopied, setFormCopied] = useState(false);

  // Table generator state
  const [tablePrompt, setTablePrompt] = useState('');
  const [tableEntityType, setTableEntityType] = useState('');
  const [tableLoading, setTableLoading] = useState(false);
  const [generatedTable, setGeneratedTable] = useState<GeneratedTableSchema | null>(null);
  const [tableCopied, setTableCopied] = useState(false);
  const [tableData, setTableData] = useState(sampleTableData);

  // Action dialog state
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [selectedRow, setSelectedRow] = useState<Record<string, unknown> | null>(null);

  const handleGenerateForm = async () => {
    if (!formPrompt.trim()) {
      toast.error('Please enter a description for the form');
      return;
    }

    setFormLoading(true);
    try {
      const result = await aiService.generateForm({
        prompt: formPrompt,
        entityType: formEntityType || undefined,
      });
      setGeneratedForm(result);
      toast.success('Form generated successfully!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to generate form');
    } finally {
      setFormLoading(false);
    }
  };

  const handleGenerateTable = async () => {
    if (!tablePrompt.trim()) {
      toast.error('Please enter a description for the table');
      return;
    }

    setTableLoading(true);
    try {
      const result = await aiService.generateTable({
        prompt: tablePrompt,
        entityType: tableEntityType || undefined,
      });
      setGeneratedTable(result);
      toast.success('Table generated successfully!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to generate table');
    } finally {
      setTableLoading(false);
    }
  };

  const handleCopySchema = (schema: object, type: 'form' | 'table') => {
    navigator.clipboard.writeText(JSON.stringify(schema, null, 2));
    if (type === 'form') {
      setFormCopied(true);
      setTimeout(() => setFormCopied(false), 2000);
    } else {
      setTableCopied(true);
      setTimeout(() => setTableCopied(false), 2000);
    }
    toast.success('Schema copied to clipboard!');
  };

  const handleDownloadSchema = (schema: object, filename: string) => {
    const blob = new Blob([JSON.stringify(schema, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleFormSubmit = (data: Record<string, unknown>) => {
    console.log('Form submitted:', data);
    toast.success('Form submitted! Check console for data.');
  };

  const handleTableAction = (action: string, row?: Record<string, unknown>) => {
    console.log('Table action:', action, row);

    switch (action) {
      case 'create':
        setSelectedRow(null);
        setCreateDialogOpen(true);
        break;
      case 'edit':
        setSelectedRow(row || null);
        setEditDialogOpen(true);
        break;
      case 'delete':
        setSelectedRow(row || null);
        setDeleteDialogOpen(true);
        break;
      default:
        toast.success(`Action: ${action}`);
    }
  };

  const handleDelete = () => {
    if (selectedRow) {
      setTableData(prev => prev.filter(item => item.id !== selectedRow.id));
      toast.success('Item deleted successfully!');
    }
    setDeleteDialogOpen(false);
    setSelectedRow(null);
  };

  const handleEditSave = () => {
    toast.success('Changes saved successfully!');
    setEditDialogOpen(false);
    setSelectedRow(null);
  };

  const handleCreate = () => {
    const newItem = {
      id: Math.max(...tableData.map(d => d.id as number)) + 1,
      name: `Item ${tableData.length + 1}`,
      status: 'Active',
      createdAt: new Date().toISOString().split('T')[0],
    };
    setTableData(prev => [...prev, newItem]);
    toast.success('Item created successfully!');
    setCreateDialogOpen(false);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold flex items-center gap-2">
          <Sparkles className="h-6 w-6 text-primary" />
          {t('ai.generator.title')}
        </h1>
        <p className="text-muted-foreground">{t('ai.generator.description')}</p>
      </div>

      <Tabs defaultValue="form" value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full max-w-md grid-cols-2">
          <TabsTrigger value="form" className="flex items-center gap-2">
            <FormInput className="h-4 w-4" />
            {t('ai.generator.formTab')}
          </TabsTrigger>
          <TabsTrigger value="table" className="flex items-center gap-2">
            <Table2 className="h-4 w-4" />
            {t('ai.generator.tableTab')}
          </TabsTrigger>
        </TabsList>

        {/* Form Generator Tab */}
        <TabsContent value="form" className="space-y-6 mt-6">
          <div className="grid gap-6 lg:grid-cols-2">
            {/* Input Section */}
            <Card>
              <CardHeader>
                <CardTitle>{t('ai.generator.formInput')}</CardTitle>
                <CardDescription>{t('ai.generator.formInputDesc')}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="formPrompt">{t('ai.generator.prompt')}</Label>
                  <Textarea
                    id="formPrompt"
                    placeholder={t('ai.generator.formPromptPlaceholder')}
                    value={formPrompt}
                    onChange={(e) => setFormPrompt(e.target.value)}
                    rows={4}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="formEntityType">{t('ai.generator.entityType')}</Label>
                  <Input
                    id="formEntityType"
                    placeholder={t('ai.generator.entityTypePlaceholder')}
                    value={formEntityType}
                    onChange={(e) => setFormEntityType(e.target.value)}
                  />
                </div>
                <Button
                  onClick={handleGenerateForm}
                  disabled={formLoading}
                  className="w-full"
                >
                  {formLoading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      {t('ai.generator.generating')}
                    </>
                  ) : (
                    <>
                      <Sparkles className="mr-2 h-4 w-4" />
                      {t('ai.generator.generateForm')}
                    </>
                  )}
                </Button>
              </CardContent>
            </Card>

            {/* Preview Section */}
            <div className="space-y-4">
              {generatedForm ? (
                <>
                  <div className="flex items-center justify-between">
                    <h3 className="text-lg font-semibold">{t('ai.generator.preview')}</h3>
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleCopySchema(generatedForm, 'form')}
                      >
                        {formCopied ? (
                          <Check className="h-4 w-4" />
                        ) : (
                          <Copy className="h-4 w-4" />
                        )}
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleDownloadSchema(generatedForm, 'form-schema.json')}
                      >
                        <Download className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                  <DynamicForm schema={generatedForm} onSubmit={handleFormSubmit} />
                </>
              ) : (
                <Card className="flex items-center justify-center h-[400px]">
                  <div className="text-center text-muted-foreground">
                    <FormInput className="h-12 w-12 mx-auto mb-4 opacity-50" />
                    <p>{t('ai.generator.noFormYet')}</p>
                  </div>
                </Card>
              )}
            </div>
          </div>
        </TabsContent>

        {/* Table Generator Tab */}
        <TabsContent value="table" className="space-y-6 mt-6">
          <div className="grid gap-6 lg:grid-cols-2">
            {/* Input Section */}
            <Card>
              <CardHeader>
                <CardTitle>{t('ai.generator.tableInput')}</CardTitle>
                <CardDescription>{t('ai.generator.tableInputDesc')}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="tablePrompt">{t('ai.generator.prompt')}</Label>
                  <Textarea
                    id="tablePrompt"
                    placeholder={t('ai.generator.tablePromptPlaceholder')}
                    value={tablePrompt}
                    onChange={(e) => setTablePrompt(e.target.value)}
                    rows={4}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="tableEntityType">{t('ai.generator.entityType')}</Label>
                  <Input
                    id="tableEntityType"
                    placeholder={t('ai.generator.entityTypePlaceholder')}
                    value={tableEntityType}
                    onChange={(e) => setTableEntityType(e.target.value)}
                  />
                </div>
                <Button
                  onClick={handleGenerateTable}
                  disabled={tableLoading}
                  className="w-full"
                >
                  {tableLoading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      {t('ai.generator.generating')}
                    </>
                  ) : (
                    <>
                      <Sparkles className="mr-2 h-4 w-4" />
                      {t('ai.generator.generateTable')}
                    </>
                  )}
                </Button>
              </CardContent>
            </Card>

            {/* Preview Section */}
            <div className="space-y-4">
              {generatedTable ? (
                <>
                  <div className="flex items-center justify-between">
                    <h3 className="text-lg font-semibold">{t('ai.generator.preview')}</h3>
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleCopySchema(generatedTable, 'table')}
                      >
                        {tableCopied ? (
                          <Check className="h-4 w-4" />
                        ) : (
                          <Copy className="h-4 w-4" />
                        )}
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleDownloadSchema(generatedTable, 'table-schema.json')}
                      >
                        <Download className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                  <DynamicTable
                    schema={generatedTable}
                    data={tableData}
                    onAction={handleTableAction}
                  />
                </>
              ) : (
                <Card className="flex items-center justify-center h-[400px]">
                  <div className="text-center text-muted-foreground">
                    <Table2 className="h-12 w-12 mx-auto mb-4 opacity-50" />
                    <p>{t('ai.generator.noTableYet')}</p>
                  </div>
                </Card>
              )}
            </div>
          </div>
        </TabsContent>
      </Tabs>

      {/* Edit Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Pencil className="h-5 w-5" />
              Edit Item
            </DialogTitle>
            <DialogDescription>
              Make changes to the selected item. This is a demo of the edit action.
            </DialogDescription>
          </DialogHeader>
          {selectedRow && (
            <div className="space-y-4 py-4">
              {Object.entries(selectedRow).map(([key, value]) => (
                <div key={key} className="space-y-2">
                  <Label htmlFor={key}>{key}</Label>
                  <Input
                    id={key}
                    defaultValue={String(value)}
                    disabled={key === 'id'}
                  />
                </div>
              ))}
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleEditSave}>
              Save Changes
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle className="flex items-center gap-2">
              <Trash2 className="h-5 w-5 text-destructive" />
              Delete Item
            </AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this item? This action cannot be undone.
              {selectedRow && (
                <div className="mt-4 p-3 bg-muted rounded-md">
                  <pre className="text-sm">{JSON.stringify(selectedRow, null, 2)}</pre>
                </div>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Create Dialog */}
      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Plus className="h-5 w-5" />
              Create New Item
            </DialogTitle>
            <DialogDescription>
              Add a new item to the table. This is a demo of the create action.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="new-name">Name</Label>
              <Input id="new-name" placeholder="Enter name..." />
            </div>
            <div className="space-y-2">
              <Label htmlFor="new-status">Status</Label>
              <Input id="new-status" placeholder="Enter status..." defaultValue="Active" />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCreateDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleCreate}>
              Create
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
