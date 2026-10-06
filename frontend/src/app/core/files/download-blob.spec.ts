import { downloadBlob } from './download-blob';

describe('downloadBlob', () => {
  const original = {
  create: URL.createObjectURL,
    revoke: URL.revokeObjectURL,
};

beforeEach(() => {
  URL.createObjectURL = vi.fn(() => 'blob:fiche');
  URL.revokeObjectURL = vi.fn();
});

afterEach(() => {
  URL.createObjectURL = original.create;
  URL.revokeObjectURL = original.revoke;
  vi.restoreAllMocks();
});

it('déclenche le téléchargement sous le nom demandé', () => {
  const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
  const blob = new Blob(['pdf'], { type: 'application/pdf'});

  downloadBlob(blob, 'fiche.pdf');

  expect(URL.createObjectURL).toHaveBeenCalledWith(blob);
  expect(click).toHaveBeenCalledTimes(1);
  const link = click.mock.contexts[0] as HTMLAnchorElement;
  expect(link.download).toBe('fiche.pdf');
  expect(link.href).toBe('blob:fiche');
});

it("libère l'adresse temporaire après le téléchargement", () => {
  vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});

  downloadBlob(new Blob(['pdf']), 'fiche.pdf');

  expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:fiche');
});
});
